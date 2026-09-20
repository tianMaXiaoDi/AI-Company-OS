# Customer Service Agent AI Governance Policy v1

Status: adopted on 2026-08-23

## Core rule

The AI may understand, classify, extract parameters, and draft responses. The service owns identity, authorization, business facts, tool selection, execution, and auditing. Model output is never an authorization or factual source.

## Allowed intent policy

| Intent | Action class | Server-owned allowed tools | Execution boundary |
| --- | --- | --- | --- |
| `SHIPPING_STATUS` | Authorized read | `getShippingStatus` | Read only after server-side order ownership verification. |
| `KNOWLEDGE_ANSWER` | Authorized read | `searchKnowledge` | Reads only published curated chunks and returns their source citation and verification date. |
| `REFUND_STATUS_EXPLANATION` | Authorized read | `getRefundStatus` | Reads only the authenticated customer's customer-safe refund case after server-side order ownership verification. |
| `HUMAN_HANDOFF` | Low-risk write | `createTicket` | Creates an `OPEN` support ticket only. |
| `REFUND_REVIEW_REQUIRED` | Human approval required | Optional `getOrder` read | May prepare a review request; cannot execute a refund. |
| `UNSUPPORTED` | No action | None | Explains the limit or routes to a human. |

## Prohibited capabilities

- Direct access to repositories, SQL, administration APIs, or any non-allow-listed tool.
- Refunds, compensation, payment changes, order cancellation, address changes, reshipment, or promotion issuance.
- Access to another customer's data or bypassing the server-side ownership check.
- Trusting a customer-supplied identity, role, authorization claim, or business fact.
- Answering policy, price, delivery, or commitment questions without a verifiable retrieval source.
- Sending customer messages, orders, or policy documents to a remote embedding provider. The opt-in semantic profile accepts loopback endpoints only until a separate provider and data-processing ADR is approved.

## Local LLM decision contract

When the explicit local `llm` profile is enabled, Ollama may emit only `StructuredAgentDecision`; it cannot name a tool or an executable business action. The current deterministic engine remains the mandatory fallback.

```json
{
  "intent": "SHIPPING_STATUS",
  "orderId": "ORD-10086",
  "handoffReason": null
}
```

The server maps the intent to its fixed tool allow list. The model's JSON must contain only the three expected fields, use a known intent, and may reference only an order ID literally present in the customer message. `SHIPPING_STATUS` requires such an ID; ticket text is always the original customer message, never model-authored text. A direct refund request cannot be downgraded to a read-only lookup, and malformed, unsafe, or unavailable local-model output reverts to deterministic routing.

`AgentDecisionEngine` is the provider-neutral port. Each request records `LLM_STRUCTURED`, `DETERMINISTIC`, or `DETERMINISTIC_FALLBACK` through its `traceId`, separately from the tool/execution audit event. Before a cited RAG capability exists, knowledge questions remain `UNSUPPORTED`.

## Local grounded answer generation

The optional `llm` profile may send a customer question and the already retrieved, published knowledge text to a loopback-only Ollama endpoint. It may draft a concise answer but cannot access tools, repositories, order data, customer identity, or unpublished documents.

The service provides source IDs with the retrieved chunks and validates the model's structured response before returning it. A response with a missing, duplicate, malformed, or unknown citation is discarded and the service returns the original cited source text instead. Citations in the API response are always server-owned metadata, never model-authored facts.

## Refund status explanation

`getRefundStatus` is a server-owned, read-only tool. It first verifies the current customer's ownership of the order, then returns only customer-safe refund status fields from the approved refund read model. It never exposes repositories, SQL, operations consoles, internal notes, or data belonging to another customer.

When the optional local `llm` profile is enabled, Qwen receives only that minimized snapshot and the customer's question; it has no database or tool access. Its JSON response must cite one or more of the server-issued fact IDs (`F1`–`F4`). Missing, duplicate, malformed, or unknown IDs are rejected and the deterministic fact rendering is returned instead. This is citation/shape validation, not independent semantic proof of natural-language wording; the server facts remain the authoritative source. The model cannot issue SQL, select arbitrary tools, or approve, reject, or execute a refund.
