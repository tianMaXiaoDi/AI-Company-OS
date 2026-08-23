# ADR-002: Customer Service Agent Uses Server-Owned Tool Governance

Date: 2026-08-23  
Status: Accepted

## Context

The Customer Service Agent will later gain LLM, RAG, and structured tool-calling capabilities. A model can be inaccurate and has no authority to access data or execute business operations. Allowing it to choose arbitrary tools or perform high-risk writes would create authorization, data-leakage, and audit risks.

## Decision

1. An LLM may produce only `StructuredAgentDecision`: an allow-listed intent, an order ID, and an optional handoff reason. It cannot select a tool, issue SQL, or assert permissions.
2. `AgentActionPolicy` maps each intent to a server-owned allow list and rejects every tool outside that list.
3. Order access continues through `CustomerSupportTools` and `OrderService`, which enforce server-side ownership checks.
4. Shipping is an authorized read; human handoff is limited to ticket creation; refund remains human-approval-only; unsupported requests perform no action.
5. Refunds, compensation, payment changes, cancellation, address changes, reshipment, and promotion issuance have no callable tool.
6. Knowledge answers require verifiable retrieval sources. Until RAG with citations is available, these requests are unsupported or handed to a human.
7. Decisions, tool calls, refusals, and approval-state changes must be traceable through audit events.

## Consequences

- Model vendors and prompts can change without increasing runtime authority.
- A future adapter must add strict JSON parsing, Bean Validation, source citations, and offline evaluation.
- A refund executor may be introduced only after a separate approval state machine and human approval interface exist.
