# Refund Status Tool Calling

`RefundStatusTool` is a read-only, deterministic Java tool that demonstrates the data boundary for a customer-service agent. Its input schema is `{"orderId":"ORD-..."}`. Its response contains only tool-produced facts: `orderFound`, `refundStatus`, `refundAmount`, and `expectedArrivalAt`.

## Calling flow

```text
Customer question
  -> LLM selects REFUND_STATUS_EXPLANATION (or deterministic fallback does)
  -> server validates that an order ID was provided
  -> authorized Java refund-status tool is called
  -> tool result is the only source for the customer-facing answer
```

The active agent follows this pattern through `CustomerSupportAgent` and the server-owned `CustomerSupportTools` boundary. The standalone `RefundStatusTool` uses fixed Mock data so it can be demoed without a database:

| orderId | result |
| --- | --- |
| `ORD-REFUND-PROCESSING` | `PROCESSING`, 88.50, expected 2026-09-23T10:00:00Z |
| `ORD-REFUND-COMPLETED` | `COMPLETED`, 129.00, arrived 2026-09-18T10:00:00Z |
| any other ID | explicit `orderFound=false`; no status, amount, or date is fabricated |

If an order ID is absent, the agent asks for it and does not call the tool. Production lookups additionally verify customer ownership before returning any refund data. If a tool times out or fails, the agent must return a safe retry or human-handoff response rather than inventing refund state.
