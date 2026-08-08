# 运行指标定义（v0）

| 指标 | 定义 | 维度 |
| --- | --- | --- |
| `active_tasks` | 当前处于运行或等待状态的任务数 | agent、workflow、status、instance |
| `task_duration_ms` | 从创建到结束的总耗时 | agent、workflow、outcome |
| `stage_duration_ms` | 模型、工具、网络、人工审批各阶段耗时 | stage、tool、agent |
| `task_success_rate` | 成功任务 / 已结束任务 | agent、workflow、版本 |
| `human_intervention_rate` | 进入人工审核的任务比例 | workflow、风险等级 |
| `task_cost` | 单任务 Token、API 与基础资源成本 | model、tool、workflow |
| `session_affinity_rate` | 使用已有会话上下文实例完成的任务比例 | route、instance、fallback |
| `context_recovery_rate` | 实例切换后成功恢复上下文的比例 | failure_type、route |

## 首批告警

- `active_tasks` 持续高于容量阈值：扩容或限流。
- P95 `task_duration_ms` 超过目标：检查工具、模型、队列和人工审批阶段。
- `task_success_rate` 回归：停止发布并回滚 Agent/Workflow 版本。
- `session_affinity_rate` 下降或上下文恢复失败：检查会话存储与路由。
- `task_cost` 超预算：降级模型、限制重试或进入人工队列。
