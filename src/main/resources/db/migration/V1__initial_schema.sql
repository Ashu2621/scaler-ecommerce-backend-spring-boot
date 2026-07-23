CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
);

CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    sku VARCHAR(64) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    stock_quantity INT NOT NULL,
    image_url VARCHAR(2048),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    category_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT chk_products_price CHECK (price > 0),
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_products_title ON products(title);
CREATE INDEX idx_products_category ON products(category_id);

CREATE TABLE app_users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    CONSTRAINT uk_app_users_email UNIQUE (email)
);

CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users(id)
);

CREATE TABLE customer_orders (
    id VARCHAR(36) PRIMARY KEY,
    customer_email VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    CONSTRAINT uk_orders_customer_idempotency UNIQUE (customer_email, idempotency_key)
);

CREATE INDEX idx_orders_customer_created ON customer_orders(customer_email, created_at);

CREATE TABLE order_items (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    sku VARCHAR(64) NOT NULL,
    product_title VARCHAR(200) NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES customer_orders(id),
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order ON order_items(order_id);

CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_reference VARCHAR(255) NOT NULL,
    checkout_url VARCHAR(2048),
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    last_reconciled_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    CONSTRAINT uk_payments_order UNIQUE (order_id),
    CONSTRAINT uk_payments_provider_reference UNIQUE (provider, provider_reference),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES customer_orders(id)
);

CREATE INDEX idx_payments_status_updated ON payments(status, updated_at);

CREATE TABLE processed_webhook_events (
    id VARCHAR(36) PRIMARY KEY,
    provider VARCHAR(30) NOT NULL,
    provider_event_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    CONSTRAINT uk_webhook_provider_event UNIQUE (provider, provider_event_id)
);
