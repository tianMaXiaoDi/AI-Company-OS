CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE orders (
    id VARCHAR(64) PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    product VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    carrier VARCHAR(80),
    tracking_number VARCHAR(80),
    estimated_delivery VARCHAR(32) NOT NULL
);

CREATE INDEX orders_customer_id_idx ON orders (customer_id);

CREATE TABLE support_tickets (
    id VARCHAR(64) PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX support_tickets_customer_id_idx ON support_tickets (customer_id);

CREATE TABLE agent_audit_events (
    id UUID PRIMARY KEY,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    session_id VARCHAR(128) NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    workflow VARCHAR(80) NOT NULL,
    tool_name VARCHAR(80) NOT NULL,
    outcome VARCHAR(80) NOT NULL
);

CREATE INDEX agent_audit_events_trace_id_idx ON agent_audit_events (trace_id);
CREATE INDEX agent_audit_events_customer_id_idx ON agent_audit_events (customer_id);
