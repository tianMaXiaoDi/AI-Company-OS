# ADR-009: Use Local LLM Structured Intent Routing With Server-Side Guards

Date: 2026-08-25  
Status: Accepted

## Context

The deterministic router demonstrates the business flow but recognizes only a narrow set of phrases. The next planned capability is LLM structured intent routing and controlled tool calling. Letting an LLM select a tool, generate an order ID, or write ticket content would make it an authorization and execution boundary.

## Decision

1. Under the explicit `llm` profile, use loopback-only Ollama as the primary `AgentDecisionEngine`. The model returns exactly `intent`, `orderId`, and `handoffReason`; it never receives tool definitions, customer identity, business data, or database access.
2. The server validates the JSON object, the allow-listed intent enum, and that a non-null order ID exactly occurs in the original customer message. Shipping requires an explicit order ID; `KNOWLEDGE_ANSWER`, `UNSUPPORTED`, and human handoff cannot carry one.
3. The deterministic engine is always executed as a fallback baseline. Any direct refund request classified by that baseline as `REFUND_REVIEW_REQUIRED` cannot be downgraded by the model. An explicit human-handoff request cannot be routed away from handoff.
4. Model-authored `handoffReason` is discarded. When a human handoff is permitted, the server writes the original customer message to the ticket. `AgentActionPolicy` remains the sole mapping from intent to allowed tools, and service-side authorization occurs before every business read or write.
5. Invalid output, an unavailable local model, or any rejected safety condition uses the deterministic decision and is auditable as `DETERMINISTIC_FALLBACK`. Accepted local decisions are auditable as `LLM_STRUCTURED`.

## Consequences

- Natural-language phrasing can influence intent selection without giving the model business-system capabilities.
- The model may still be wrong semantically; validation is a boundary check, not proof of intent quality. Offline evaluation and prompt-injection cases must measure quality before broadening tool coverage.
- Existing `dev` runs remain deterministic. Only `dev,semantic,llm` activates the local classifier and remains resilient when Ollama is unavailable.
