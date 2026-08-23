# Customer Service Agent 合同

## 职责

- 识别当前 MVP 支持的订单/物流查询和人工客服意图。
- 在订单所有权校验通过后查询订单或物流状态。
- 为需要人工处理的问题创建工单。
- 为每次处理记录可关联的审计事件。

## 非职责

- 不执行退款、改地址、改订单或任何支付操作。
- 不访问其他客户的订单。
- 不把模型生成的内容当作业务事实；订单数据必须来自工具。
- 当前版本不回答没有可核验来源的政策问题。

## 输入

```yaml
customer_id: CUST-1001          # 来自认证层；示例由 X-Customer-Id 提供
session_id: optional-session-id
message: 我的订单 ORD-10086 到哪里了？
```

## 输出

```yaml
intent: SHIPPING_STATUS
response: "订单 ORD-10086 已由 DHL 发货，预计 2026-09-02 送达。"
tools_called:
  - getShippingStatus
trace_id: uuid
```

## 工具与权限

| 工具 | 权限 | 失败行为 |
| --- | --- | --- |
| `getOrder` / `getShippingStatus` | 仅订单所属客户 | 返回 `403`，不泄露订单存在性或详情 |
| `createTicket` | 已认证客户 | 返回工单 ID；写入审计事件 |
| `refund` | 禁止 | 由未来人工审批工作流负责 |

## 人工审批点

- 退款、订单修改、支付和其他高风险操作必须进入未来的审批工作流。
- 当前版本仅能创建人工支持工单。
