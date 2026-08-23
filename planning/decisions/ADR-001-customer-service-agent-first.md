# ADR-001：以 AI Customer Service Agent 作为首个产品化模块

日期：2026-08-22  
状态：已采纳

## 背景

AI Company OS 规划了客服、知识中枢、Agent Engine、销售研究和可观测性五个模块。并行启动会产生多个无法验证的 Demo，且难以沉淀共享的权限、工具和审计能力。

## 决策

先交付 AI Customer Service Agent，并按以下顺序扩展：

1. 订单/物流查询、人工工单、鉴权与审计；
2. 企业知识库 RAG；
3. LLM 结构化意图与受控 Tool Calling；
4. 退款建议与人工审批；
5. 将成熟的工具、状态和观测能力抽到通用 Agent Engine。

v0.1 以无外部依赖的 Java 可运行 Demo 验证确定性业务边界。该链路已通过验证，v0.2 已迁移至 Spring Boot + PostgreSQL + Redis + Docker Compose，并保留订单授权和人工审核边界。

## 后果

- 优先积累一个完整、可演示、具备业务约束的项目。
- Knowledge Agent 与 Agent Engine 的公共能力会从真实客服需求中提炼，而非预先抽象。
- 初期会暂时使用内存示例数据，不能被误认为生产实现。
