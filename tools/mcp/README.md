# MCP 工具层规划

此目录用于存放 MCP Server、配置示例、集成说明和运行脚本。密钥只放本地 `.env`，不得提交。

## 第一个候选：Observability MCP Server（只读）

优先实现一个为 AI Company OS 运行治理服务的只读 MCP Server，而不是先连接高风险外部系统。

| MCP Tool | 输入 | 输出 | 风险/权限 |
| --- | --- | --- | --- |
| `get_task_status` | `task_id` | 状态、阶段、耗时、错误摘要 | 只读；需任务可见性校验 |
| `list_active_tasks` | `workflow_id?`、`limit` | 当前排队/运行/等待任务 | 只读；限流、脱敏 |
| `get_session_trace` | `session_id` | 脱敏事件时间线 | 只读；按用户/项目隔离 |
| `get_workflow_metrics` | `workflow_id`、时间范围 | 成功率、P95、成本、人工介入率 | 只读；聚合数据优先 |

## Resources

- `company-os://observability/metric-definitions`：指标定义。
- `company-os://observability/current-summary`：当前运行总览（脱敏、只读）。

## 安全清单

- 默认只读，拒绝任何未注册的写操作。
- 每次工具调用校验身份、项目归属、输入 Schema 和授权范围。
- 所有调用进入 `observability/` 事件链；日志只保留脱敏参数和结果摘要。
- 返回内容不应把不可信资源文本当作指令执行，防范 Prompt Injection。
- 写操作必须单独设计审批、幂等、回滚和审计，不能由只读 Server 临时升级。
