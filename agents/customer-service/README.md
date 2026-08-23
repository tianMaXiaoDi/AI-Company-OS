# Customer Service Agent

AI governance and the future structured-decision contract: [ai-governance.md](ai-governance.md).

状态：`v0.2 — enterprise foundation`

这个 Agent 处理电商客户的订单、物流和人工客服请求。它先通过确定性规则识别有限意图，再调用受权限约束的订单或工单能力；它**不**自行编造订单状态，也不能执行退款。订单、工单和审计已迁移至 PostgreSQL；会话订单引用通过 Redis 保存。

当前可演示链路：

1. 客户查询自己订单的物流状态。
2. 系统校验订单归属并返回运输信息。
3. 客户请求人工帮助时，系统创建工单并返回工单编号。

可运行服务与接口说明见 [Customer Service Agent demo](../../demos/customer-service-agent/README.md)。

后续版本会依次增加：LLM 分类与结构化输出、受控 Tool Calling、企业知识库 RAG、退款建议及人工审批。
