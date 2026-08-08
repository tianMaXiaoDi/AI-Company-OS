# Agent Observability & Runtime Governance

这是 AI Company OS 的横向运行治理目录，承载所有 Agent 和 Workflow 的任务监控、会话管理、资源统计、追踪、告警与运行复盘能力。

## 它要回答的问题

- 当前有多少 Agent 任务正在排队、运行、等待工具、暂停、失败或完成？
- 每个任务从创建到完成耗时多久，时间花在模型、工具、网络还是人工审批？
- 每个任务调用了哪些 Agent/工具，消耗了多少 Token、CPU、内存、网络或外部 API 预算？
- 同一个 `session_id` 是否被路由到能够读取其上下文的运行实例？扩缩容、重试或故障转移后能否恢复？
- 哪些错误需要自动重试，哪些必须升级给人？

## 目录结构

```text
observability/
├── README.md
├── event-schema.md       # task/session/trace/tool 事件合同
├── metrics.md            # 指标定义、维度和告警阈值
├── routing.md            # 会话感知路由与扩缩容原则
├── dashboards/           # 看板查询和截图/链接
├── runbooks/             # 告警处理、降级、回滚、数据保留
└── experiments/          # 负载、延迟、路由和成本实验
```

## 第一版范围（M1–M3）

1. 为每次运行生成 `task_id`、`session_id`、`trace_id`。
2. 记录任务状态流转：`queued → running → waiting_tool → human_review → succeeded/failed/cancelled`。
3. 记录开始时间、结束时间、各阶段耗时、工具调用、错误与人工介入。
4. 记录 Token、模型/API 成本、并发数和基础资源使用；敏感内容只保留脱敏摘要或引用。
5. 做一个最小实时看板：运行中任务数、P50/P95 延迟、失败率、人工介入率、成本/任务。
6. 为会话感知路由写一个可复现的模拟实验：同一会话优先路由到已有上下文的实例；实例不可用时从持久化状态恢复。

## 非目标

第一版不追求一次性接入所有云厂商，也不把监控目录变成业务 Agent。先建立统一事件合同和可验证的最小运行闭环。

## 安全边界

默认不记录原始 Prompt、客户正文、订单隐私或密钥。日志按最小必要原则脱敏、分级授权和设置保留期限；涉及生产操作的告警、重试和扩缩容必须有审计记录。
