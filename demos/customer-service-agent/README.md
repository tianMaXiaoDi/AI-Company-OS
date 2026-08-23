# AI Customer Service Agent — v0.2

这是 AI Company OS 的第一个正式企业服务：一个基于 Spring Boot 的客服 Agent 基座，拥有持久化订单/工单/审计、Redis 会话记忆、受限业务工具和容器化本地环境。

## 技术栈

| 范围 | 技术 |
| --- | --- |
| 应用 | Java 21、Spring Boot 3.5.16、Spring MVC、Virtual Threads |
| 业务与数据 | Spring Data JPA、PostgreSQL 16、Flyway |
| 知识层 | PostgreSQL 中的受控知识文档和可引用文本块；当前使用无模型的词项检索基线，pgvector 已启用并会在确定 Embedding 模型维度后追加 |
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

1. 选择并评估 Embedding 模型，再将现有可引用知识块追加到 `vector(n)` 语义检索索引。
2. 将 LLM 的结构化意图输出接入 `CustomerSupportTools`，并建立离线评估集。
3. 增加退款建议与 `PENDING_APPROVAL → APPROVED/REJECTED → EXECUTED` 人工审批状态机。
