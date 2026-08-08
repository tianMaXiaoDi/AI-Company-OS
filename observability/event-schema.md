# 运行事件合同（草案）

每条事件至少包含：

```yaml
event_name: task.started
occurred_at: 2026-08-08T12:00:00Z
task_id: task_123
session_id: session_456
trace_id: trace_789
agent_id: customer-service
workflow_id: customer-refund-assist
status: running
parent_task_id: null
instance_id: runtime-a
metadata:
  input_class: refund_request
```

## 必须支持的事件

`task.created`、`task.queued`、`task.started`、`task.waiting_tool`、`tool.called`、`tool.completed`、`task.human_review`、`task.succeeded`、`task.failed`、`task.cancelled`。

## 事件设计原则

- `task_id` 标识一次任务；`session_id` 标识可持续上下文；`trace_id` 串联一次端到端执行。
- 事件不可依赖单个实例内存，必须可写入持久化事件流或数据库。
- 业务正文与运行元数据分离，默认只记录脱敏摘要、分类和引用。
- 事件追加写入，修正通过新事件表达，保留审计历史。
