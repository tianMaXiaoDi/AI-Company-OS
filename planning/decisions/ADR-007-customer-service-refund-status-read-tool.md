# ADR-007: Add a Customer-Scoped, Read-Only Refund Status Tool

Date: 2026-08-25  
Status: Accepted

## Context

Customers need an explanation when an already requested refund appears stuck. This is a real-time business-fact question, not a policy question: RAG cannot establish its answer, and an LLM must not access operations databases or consoles directly.

## Decision

1. Add `REFUND_STATUS_EXPLANATION` as an authorized-read intent, distinct from `REFUND_REVIEW_REQUIRED`, which remains human-approval-only.
2. Expose one server-owned tool, `getRefundStatus(customerId, orderId)`. The tool verifies order ownership through `OrderService` before it queries the refund read model.
3. Persist a customer-safe `refund_cases` read model containing only the refund ID, lifecycle status, safe reason code and description, next action, and update time. The agent has no write method for this model.
4. Do not expose repositories, SQL, operations consoles, internal notes, payment credentials, or free-form query capabilities to the agent or any LLM.
5. The service audits every lookup. With the optional local `llm` profile, an LLM may summarize the returned snapshot only: it must return structured JSON with an answer and server-issued fact IDs. The service rejects missing, duplicate, malformed, or unknown IDs and falls back to deterministic rendering. It cannot modify refund state or decide on an executable action.

## Consequences

- A customer can ask why an existing refund is delayed without gaining access to another customer's information.
- A request without an order reference is refused with `REFUND_REFERENCE_REQUIRED`; a customer-owned order without a refund case returns `REFUND_NOT_FOUND`.
- Refund approval, rejection, execution, compensation, and payment changes remain outside this capability and require human approval workflows.
