CREATE DATABASE IF NOT EXISTS commerce_warehouse;

CREATE EXTERNAL TABLE IF NOT EXISTS commerce_warehouse.order_events (
    event_id STRING,
    order_id STRING,
    customer_id STRING,
    status STRING,
    amount DECIMAL(19, 2),
    currency STRING,
    occurred_at TIMESTAMP
)
PARTITIONED BY (event_date DATE)
STORED AS PARQUET
LOCATION 's3://REPLACE_WITH_DATA_LAKE/curated/order_events';

-- With dynamic partitioning enabled, a scheduled Spark job can load new data.
INSERT OVERWRITE TABLE commerce_warehouse.order_events
PARTITION (event_date)
SELECT
    event_id,
    order_id,
    customer_id,
    status,
    amount,
    currency,
    occurred_at,
    TO_DATE(occurred_at) AS event_date
FROM commerce_warehouse.order_events_staging;

ANALYZE TABLE commerce_warehouse.order_events
PARTITION (event_date)
COMPUTE STATISTICS;
