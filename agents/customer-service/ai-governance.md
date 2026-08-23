# Customer Service Agent AI Governance Policy v1

Status: adopted on 2026-08-23

## Core rule

The AI may understand, classify, extract parameters, and draft responses. The service owns identity, authorization, business facts, tool selection, execution, and auditing. Model output is never an authorization or factual source.

## Allowed intent policy

| Intent | Action class | Server-owned allowed tools | Execution boundary |
| --- | --- | --- | --- |
| `SHIPPING_STATUS` | Authorized read | `getShippingStatus` | Read only after server-side order ownership verification. |
| `HUMAN_HANDOFF` | Low-risk write | `createTicket` | Creates an `OPEN` support ticket only. |
| `REFUND_REVIEW_REQUIRED` | Human approval required | Optional `getOrder` read | May prepare a review request; cannot execute a refund. |
| `UNSUPPORTED` | No action | None | Explains the limit or routes to a human. |

## Prohibited capabilities

- Direct access to repositories, SQL, administration APIs, or any non-allow-listed tool.
- Refunds, compensation, payment changes, order cancellation, address changes, reshipment, or promotion issuance.
- Access to another customer's data or bypassing the server-side ownership check.
- Trusting a customer-supplied identity, role, authorization claim, or business fact.
- Answering policy, price, delivery, or commitment questions without a verifiable retrieval source.

## Future LLM decision contract

An LLM adapter may emit only `StructuredAgentDecision`; it cannot name a tool or an executable business action.

```json
{
  "intent": "SHIPPING_STATUS",
  "orderId": "ORD-10086",
  "handoffReason": null
}
```

The server maps the intent to its fixed tool allow list. `AgentDecisionEngine` is the provider-neutral port: the current deterministic implementation and a future Spring AI implementation both return this same contract. Every decision, tool call, refusal, and approval-state change must be linked through `traceId` in the audit trail. Before a cited RAG capability exists, knowledge questions remain `UNSUPPORTED`.
