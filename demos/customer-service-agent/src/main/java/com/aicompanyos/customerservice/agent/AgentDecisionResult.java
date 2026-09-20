package com.aicompanyos.customerservice.agent;

import java.util.Objects;

/** A constrained decision plus server-owned provenance for the audit trail. */
public record AgentDecisionResult(StructuredAgentDecision decision, DecisionSource source) {
    public AgentDecisionResult {
        Objects.requireNonNull(decision, "decision is required");
        Objects.requireNonNull(source, "source is required");
    }
}
