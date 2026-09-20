CREATE TABLE refund_cases (
    id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL UNIQUE REFERENCES orders (id),
    status VARCHAR(64) NOT NULL,
    reason_code VARCHAR(80) NOT NULL,
    reason_description VARCHAR(1000) NOT NULL,
    next_action VARCHAR(80) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX refund_cases_order_id_idx ON refund_cases (order_id);
