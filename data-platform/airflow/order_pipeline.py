"""Airflow 3 DAG that orchestrates validation, Spark transformation, and quality checks."""

from datetime import datetime

from airflow.sdk import dag, task


@dag(
    dag_id="commerce_order_pipeline",
    schedule="@hourly",
    start_date=datetime(2026, 1, 1),
    catchup=False,
    max_active_runs=1,
    tags=["commerce", "spark", "quality"],
)
def commerce_order_pipeline():
    @task
    def resolve_partition(data_interval_start=None) -> str:
        return data_interval_start.strftime("%Y-%m-%d-%H")

    @task.bash
    def run_spark(partition: str) -> str:
        return (
            "spark-submit /opt/commerce/order_analytics.py "
            f"--input s3a://commerce-raw/orders/hour={partition} "
            f"--output s3a://commerce-curated/orders/hour={partition}"
        )

    @task
    def quality_gate(partition: str) -> None:
        # Production version queries the warehouse and fails on null IDs,
        # negative amounts, duplicates, or an unexpected volume drop.
        if not partition:
            raise ValueError("Partition must not be empty")

    selected = resolve_partition()
    run_spark(selected) >> quality_gate(selected)


commerce_order_pipeline()
