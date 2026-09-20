INSERT INTO refund_cases (id, order_id, status, reason_code, reason_description, next_action, updated_at)
VALUES
    ('RF-10001', 'ORD-10086', 'PENDING_MANUAL_REVIEW', 'PAYMENT_RISK_REVIEW',
     '支付渠道要求补充人工风控审核，退款尚未进入打款阶段。', 'WAIT_FOR_OPERATIONS_REVIEW',
     TIMESTAMP WITH TIME ZONE '2026-08-24 20:30:00+08');
