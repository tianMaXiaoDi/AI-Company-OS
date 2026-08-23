INSERT INTO knowledge_documents (id, source_key, title, source_uri, published_at, verified_at, status)
VALUES ('1500c3db-19b2-4b08-9694-1c4bc2e97e72', 'customer-service-policy-v1', '客户服务政策 v1', 'repository://knowledge/customer-service/policies-v1.md', DATE '2026-08-23', DATE '2026-08-23', 'PUBLISHED');

INSERT INTO knowledge_chunks (id, document_id, chunk_index, citation_anchor, content)
VALUES
    ('2686076c-2c4a-4d10-8f52-87a0ca4d3355', '1500c3db-19b2-4b08-9694-1c4bc2e97e72', 1, 'refunds-and-order-changes',
     '退款申请必须进入人工审核，客服 Agent 不会自动执行退款。订单取消、地址修改、补偿和支付变更同样需要人工处理。'),
    ('1fd5c9ad-a67d-4bd0-afd9-e28be5df50f9', '1500c3db-19b2-4b08-9694-1c4bc2e97e72', 2, 'order-and-shipping-queries',
     '客户可以查询本人订单的物流状态。物流、承运商和预计送达时间必须来自订单服务，不由知识库或模型生成。'),
    ('d634a4c8-cdb3-4b54-a760-5be5d5e45a10', '1500c3db-19b2-4b08-9694-1c4bc2e97e72', 3, 'human-support',
     '需要人工处理时，已认证客户可以创建人工支持工单。工单创建后状态为 OPEN，由客服继续跟进。');
