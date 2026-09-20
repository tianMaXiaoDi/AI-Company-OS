# AI Customer Service Agent — v0.2

这是 AI Company OS 的第一个正式企业服务：一个基于 Spring Boot 的客服 Agent 基座，拥有持久化订单/工单/审计、Redis 会话记忆、受限业务工具和容器化本地环境。

## 技术栈

| 范围 | 技术 |
| --- | --- |
| 应用 | Java 21、Spring Boot 3.5.16、Spring MVC、Virtual Threads |
| 业务与数据 | Spring Data JPA、PostgreSQL 16、Flyway |
| 知识层 | PostgreSQL 中的受控知识文档、可引用文本块和 pgvector HNSW 索引；默认使用无模型的词项检索，可选本地 BAAI/bge-m3 语义检索 |
| 会话 | Redis 7，保存最小的会话订单引用，TTL 为 8 小时 |
| 运行治理 | Spring Boot Actuator、Prometheus endpoint、持久化 append-only 审计事件 |
| 部署 | Docker Compose、多阶段 Docker build、非 root 运行用户 |
| 测试 | JUnit 5、Mockito、AssertJ |

Spring Boot 3.5.16 支持 Java 17–25；项目编译目标为 Java 21。[官方系统要求](https://docs.spring.io/spring-boot/3.5/system-requirements.html)

## 架构边界

```text
HTTP / Chat API
       │
CustomerSupportAgent ── RedisConversationMemory
       │
CustomerSupportTools  ← 唯一允许 Agent 调用的业务工具边界
       │
OrderService / TicketService / AuditService
       │
PostgreSQL + pgvector          Redis
```

Agent 当前用可测试的确定性路由验证流程。政策问题只从已发布知识块中返回原始受控文本、来源和验证日期；没有来源时明确拒答。未来 LLM 只能输出受限意图或调用 `CustomerSupportTools`，不能直连 Repository、越过订单授权，或执行退款。

### 可选本地语义检索

默认 `dev` profile 不调用任何 Embedding 服务。若本机已部署兼容 OpenAI `/v1/embeddings` 协议的 `BAAI/bge-m3` 服务，可显式启用 `dev,semantic` profile，并设定 `CUSTOMER_SERVICE_KNOWLEDGE_SEMANTIC_INDEX_ON_STARTUP=true` 为已发布政策块创建 1024 维向量索引。该实现只接受 `localhost`、`127.0.0.1` 或 `::1` 端点；远端提供商尚未获批。

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev,semantic'
$env:CUSTOMER_SERVICE_KNOWLEDGE_SEMANTIC_BASE_URL = 'http://localhost:11434'
$env:CUSTOMER_SERVICE_KNOWLEDGE_SEMANTIC_INDEX_ON_STARTUP = 'true'
mvn spring-boot:run
```

### Local Ollama grounded RAG

The supported local setup uses Ollama's `bge-m3` embedding model and `qwen3:4b` chat model. Pull both once, then activate all three local profiles. The first startup creates embeddings for published knowledge chunks; later startups should omit `CUSTOMER_SERVICE_KNOWLEDGE_SEMANTIC_INDEX_ON_STARTUP` unless re-indexing is intended.

```powershell
ollama pull bge-m3
ollama pull qwen3:4b
$env:SPRING_PROFILES_ACTIVE = 'dev,semantic,llm'
$env:CUSTOMER_SERVICE_KNOWLEDGE_SEMANTIC_INDEX_ON_STARTUP = 'true'
mvn spring-boot:run
```

The `llm` profile uses Spring AI's `ChatClient` and Ollama tool-calling support for the refund-status workflow. The model receives only the `getRefundStatus(orderId)` schema. Spring AI executes the requested Java tool, appends its result to the conversation, and asks the model for the final response. The model never receives customer identity, repository access, or a free choice of order ID: the Java adapter binds every tool call to the authenticated customer and the order ID already present in the original message. The returned JSON answer must cite server facts; otherwise the service falls back to the deterministic reply. The former hand-written Ollama adapters remain available only under the explicit `legacy-llm` profile.

### Refund Status Tool Calling

For refund-status questions, the LLM (or the deterministic fallback) may select the `REFUND_STATUS_EXPLANATION` intent only; the server, not the model, decides whether the authorized Java tool can run. If no order ID is present, the agent asks the customer to provide one. With an order ID, the flow is `question → validated intent → Java tool → customer-safe answer`. The answer is grounded only in tool results, so the model cannot invent a refund status, amount, or expected arrival time.

`RefundStatusTool` is a standalone in-memory demo tool with three deterministic outcomes: a refund in progress, a completed refund, and an explicit order-not-found response. Its input is `orderId`; its output is `orderFound`, `refundStatus`, `refundAmount`, and `expectedArrivalAt`. The active agent uses the same guarded pattern through `CustomerSupportTools`, where customer ownership is verified before business data is returned. See [the detailed flow](docs/refund-status-tool-calling.md).

## 本地启动

先启动 Docker Desktop，然后在此目录运行：

```powershell
docker compose up --build
```

服务地址：`http://localhost:8080`。健康检查：`http://localhost:8080/actuator/health`；Prometheus 指标：`http://localhost:8080/actuator/prometheus`。

停止并保留数据：

```powershell
docker compose down
```

停止并删除本地数据库卷（会清除 Demo 数据）：

```powershell
docker compose down --volumes
```

## 验证 API

```powershell
# 查询自己的订单
Invoke-RestMethod http://localhost:8080/api/orders/ORD-10086 -Headers @{ 'X-Customer-Id' = 'CUST-1001' }

# 工具驱动的物流回答
Invoke-RestMethod http://localhost:8080/api/chat -Method Post -ContentType 'application/json' -Headers @{ 'X-Customer-Id' = 'CUST-1001' } -Body '{"sessionId":"demo-1","message":"我的订单 ORD-10086 到哪里了？"}'

# 会话记忆 + 退款人工审核边界
Invoke-RestMethod http://localhost:8080/api/chat -Method Post -ContentType 'application/json' -Headers @{ 'X-Customer-Id' = 'CUST-1001' } -Body '{"sessionId":"demo-1","message":"那可以退款吗？"}'

# 带来源的政策回答（不调用外部模型）
Invoke-RestMethod http://localhost:8080/api/chat -Method Post -ContentType 'application/json' -Headers @{ 'X-Customer-Id' = 'CUST-1001' } -Body '{"sessionId":"demo-1","message":"订单取消政策是什么？"}'
```

`X-Customer-Id` 是开发环境身份模拟，不是真实认证。生产部署前必须用 JWT/OIDC claim 适配器替换，且必须在网关/服务端验证 token；客户端不得直接声明 customer ID。

## 测试

安装 Maven 3.6.3+ 后：

```powershell
mvn test
```

测试覆盖：受信任工具返回物流事实、跨客户拒绝、人工工单和退款不自动执行。

## 下一迭代

1. 用离线评估集验证词项和本地语义检索的召回、引用正确性与拒答率，再决定是否扩大政策语料。
2. 将 LLM 的结构化意图输出接入 `CustomerSupportTools`，并建立离线评估集。
3. 增加退款建议与 `PENDING_APPROVAL → APPROVED/REJECTED → EXECUTED` 人工审批状态机。
