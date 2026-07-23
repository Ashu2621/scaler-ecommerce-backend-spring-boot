"""Batch PySpark job: raw order events -> partitioned, curated commerce metrics."""

import argparse

from pyspark.sql import SparkSession, Window
from pyspark.sql import functions as F
from pyspark.sql.types import DecimalType


def build_metrics(input_path: str, output_path: str) -> None:
    spark = (
        SparkSession.builder
        .appName("commerce-order-analytics")
        .config("spark.sql.adaptive.enabled", "true")
        .config("spark.sql.sources.partitionOverwriteMode", "dynamic")
        .getOrCreate()
    )

    orders = (
        spark.read.parquet(input_path)
        .select(
            "order_id",
            "customer_id",
            "status",
            F.col("amount").cast(DecimalType(19, 2)).alias("amount"),
            "currency",
            F.to_timestamp("occurred_at").alias("occurred_at"),
        )
        .dropDuplicates(["order_id", "status", "occurred_at"])
        .filter(F.col("status") == "PAID")
        .withColumn("event_date", F.to_date("occurred_at"))
    )

    customer_window = Window.partitionBy("customer_id").orderBy("occurred_at")
    enriched = (
        orders
        .withColumn("customer_order_number", F.row_number().over(customer_window))
        .withColumn("previous_order_at", F.lag("occurred_at").over(customer_window))
        .withColumn(
            "days_since_previous_order",
            F.datediff("occurred_at", "previous_order_at"),
        )
    )

    daily = (
        enriched.groupBy("event_date", "currency")
        .agg(
            F.countDistinct("order_id").alias("paid_orders"),
            F.countDistinct("customer_id").alias("unique_customers"),
            F.sum("amount").alias("gmv"),
            F.avg("amount").alias("average_order_value"),
        )
    )

    (
        daily.repartition("event_date")
        .write.mode("overwrite")
        .partitionBy("event_date")
        .parquet(f"{output_path}/daily_metrics")
    )
    (
        enriched.repartition("event_date")
        .write.mode("overwrite")
        .partitionBy("event_date")
        .parquet(f"{output_path}/enriched_orders")
    )
    spark.stop()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    build_metrics(args.input, args.output)
