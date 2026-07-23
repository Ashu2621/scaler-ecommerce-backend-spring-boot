"""Structured Streaming job: Kafka order events -> watermarked hourly GMV."""

import argparse

from pyspark.sql import SparkSession
from pyspark.sql import functions as F
from pyspark.sql.types import (
    DecimalType,
    StringType,
    StructField,
    StructType,
    TimestampType,
)

EVENT_SCHEMA = StructType(
    [
        StructField("orderId", StringType(), False),
        StructField("customerEmail", StringType(), False),
        StructField("amount", DecimalType(19, 2), False),
        StructField("currency", StringType(), False),
        StructField("occurredAt", TimestampType(), False),
    ]
)


def run(bootstrap_servers: str, topic: str, checkpoint: str, output: str) -> None:
    spark = SparkSession.builder.appName("commerce-order-streaming").getOrCreate()
    raw = (
        spark.readStream.format("kafka")
        .option("kafka.bootstrap.servers", bootstrap_servers)
        .option("subscribe", topic)
        .option("startingOffsets", "latest")
        .load()
    )
    events = (
        raw.select(F.from_json(F.col("value").cast("string"), EVENT_SCHEMA).alias("event"))
        .select("event.*")
        .withWatermark("occurredAt", "15 minutes")
        .dropDuplicatesWithinWatermark(["orderId"])
    )
    hourly = (
        events.groupBy(
            F.window("occurredAt", "1 hour"),
            "currency",
        )
        .agg(
            F.countDistinct("orderId").alias("orders"),
            F.sum("amount").alias("gmv"),
        )
    )
    query = (
        hourly.writeStream
        .format("parquet")
        .outputMode("append")
        .option("path", output)
        .option("checkpointLocation", checkpoint)
        .partitionBy("currency")
        .trigger(processingTime="1 minute")
        .start()
    )
    query.awaitTermination()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--bootstrap-servers", default="localhost:9092")
    parser.add_argument("--topic", default="commerce.order-events")
    parser.add_argument("--checkpoint", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    run(args.bootstrap_servers, args.topic, args.checkpoint, args.output)
