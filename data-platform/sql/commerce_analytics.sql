-- RDBMS, joins, unions, CTEs, window functions, and interview-style analytics.
-- These queries run against the transactional schema created by Flyway.

-- Daily GMV and unique paying customers.
WITH paid_orders AS (
    SELECT
        DATE(created_at) AS order_date,
        customer_email,
        total_amount
    FROM customer_orders
    WHERE status = 'PAID'
)
SELECT
    order_date,
    COUNT(*) AS paid_orders,
    COUNT(DISTINCT customer_email) AS unique_customers,
    SUM(total_amount) AS gmv,
    AVG(total_amount) AS average_order_value
FROM paid_orders
GROUP BY order_date
ORDER BY order_date;

-- Rank products in every category by revenue.
WITH product_revenue AS (
    SELECT
        c.name AS category_name,
        oi.product_id,
        oi.product_title,
        SUM(oi.quantity) AS units_sold,
        SUM(oi.line_total) AS revenue
    FROM order_items oi
    JOIN customer_orders o ON o.id = oi.order_id
    JOIN products p ON p.id = oi.product_id
    JOIN categories c ON c.id = p.category_id
    WHERE o.status = 'PAID'
    GROUP BY c.name, oi.product_id, oi.product_title
),
ranked AS (
    SELECT
        *,
        DENSE_RANK() OVER (
            PARTITION BY category_name
            ORDER BY revenue DESC
        ) AS revenue_rank
    FROM product_revenue
)
SELECT *
FROM ranked
WHERE revenue_rank <= 3
ORDER BY category_name, revenue_rank;

-- Seven-day rolling GMV using a window frame.
WITH daily AS (
    SELECT DATE(created_at) AS day, SUM(total_amount) AS gmv
    FROM customer_orders
    WHERE status = 'PAID'
    GROUP BY DATE(created_at)
)
SELECT
    day,
    gmv,
    SUM(gmv) OVER (
        ORDER BY day
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ) AS rolling_7d_gmv
FROM daily
ORDER BY day;

-- Customer cohorts using LAG to calculate time between purchases.
SELECT
    customer_email,
    created_at,
    total_amount,
    LAG(created_at) OVER (
        PARTITION BY customer_email
        ORDER BY created_at
    ) AS previous_order_at
FROM customer_orders
WHERE status = 'PAID';

-- Portable pivot with conditional aggregation.
SELECT
    DATE(created_at) AS day,
    SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) AS paid,
    SUM(CASE WHEN status = 'PENDING_PAYMENT' THEN 1 ELSE 0 END) AS pending,
    SUM(CASE WHEN status = 'PAYMENT_FAILED' THEN 1 ELSE 0 END) AS failed
FROM customer_orders
GROUP BY DATE(created_at)
ORDER BY day;

-- UNION ALL is intentionally used: no expensive de-duplication is required.
SELECT id, created_at, 'ORDER' AS event_type
FROM customer_orders
UNION ALL
SELECT id, created_at, 'PAYMENT' AS event_type
FROM payments
ORDER BY created_at DESC;
