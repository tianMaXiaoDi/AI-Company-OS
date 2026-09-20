# ADR-008: Validate Local LLM Refund Explanations Against Server Facts

Date: 2026-08-25  
Status: Accepted

## Context

The refund-status tool supplies current, customer-authorized facts, but its deterministic wording is less natural than a conversational explanation. Giving an LLM database, repository, or operations-console access would make the model a security and authorization boundary, which is unacceptable.

## Decision

1. The optional `llm` profile calls a loopback-only Ollama endpoint after `getRefundStatus` has completed authorization and data minimization.
2. The model receives only the customer question and four customer-safe server facts: status, reason description, update time, and next action. It receives no identity data, SQL capability, repository, operations notes, or tool definitions.
3. The model must return JSON with `answer` and a non-empty `factIds` array. Each ID must be a unique member of the server-generated allow list `F1`–`F4`.
4. Invalid, unavailable, or malformed model responses are discarded. The agent returns the existing deterministic reply and audits the outcome as `DETERMINISTIC_FALLBACK`; accepted output is audited as `LLM_GROUNDED`.
5. Citation-ID validation establishes that the model selected only supplied evidence IDs. It does not independently prove every phrase of free-form natural-language text, so the server facts remain authoritative and refund mutations remain out of scope.

## Consequences

- Local Qwen can make a verified read result easier to understand without gaining business-system permissions.
- A local model outage or a bad structured response does not prevent a customer from receiving the verified deterministic result.
- Future production work should add prompt-injection testing, latency/error metrics, and a review of whether a stronger claim-to-evidence validator is needed for the risk level.
