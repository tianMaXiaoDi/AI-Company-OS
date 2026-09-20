# Changelog

## Unreleased

- Added a loopback-only Ollama structured-intent adapter. It can choose only an allow-listed, non-executable intent and an order ID that appears in the customer's own message; the deterministic router is retained as a mandatory fallback.
- Added decision-source audit events (`LLM_STRUCTURED`, `DETERMINISTIC`, or `DETERMINISTIC_FALLBACK`) and validation that protects refund-review and human-handoff boundaries from unsafe model routing.
- Added an opt-in local semantic retrieval profile backed by `pgvector` and a 1024-dimension BAAI/bge-m3-compatible embedding endpoint. It is disabled by default, rejects remote endpoints, and falls back to citation-first lexical retrieval when unavailable.
- Added a citation-first knowledge retrieval baseline. It reads only published, curated policy chunks and returns the source URI and verification date; unmatched questions remain unanswered rather than fabricated.
- Added a server-owned AI tool policy: models may emit only a constrained intent and parameters; the service controls the tool allow list and action class.
- Added the `AgentIntent`, `AgentActionPolicy`, and `StructuredAgentDecision` contracts; out-of-policy tool calls are rejected.
- Kept refunds in `HUMAN_APPROVAL_REQUIRED`; no refund execution tool exists.
- Added the provider-neutral `AgentDecisionEngine` port and moved the current rules into `DeterministicAgentDecisionEngine`; a future Spring AI adapter can replace only this decision component.
- Added explicit `dev` and `jwt` identity modes. The JWT/OIDC mode ignores `X-Customer-Id`, derives the customer from a verified token claim, and enforces a configured support authority.

## v0.2 — 2026-08-22

- 迁移至 Java 21 + Spring Boot 3.5.16；加入 Spring MVC、JPA、Flyway、Redis、Actuator 和 Prometheus 指标端点。
- 订单、工单和追加式审计事件改为 PostgreSQL 持久化；Docker Compose 包含 PostgreSQL 16 + pgvector 和 Redis。
- 会话只保存最近订单 ID，8 小时后过期；退款仍只产生人工审核建议。
- 定义 `CustomerSupportTools` 为 Agent 唯一业务工具边界，后续 LLM/MCP 适配器不得直连 Repository。

## v0.1 — 2026-08-22

- 新建受限的 Customer Service Agent MVP。
- 提供订单/物流查询、人工工单创建、订单所有权校验和审计事件。
- 未引入 LLM、RAG 或退款执行；避免在基础业务层未验证前扩大风险面。
