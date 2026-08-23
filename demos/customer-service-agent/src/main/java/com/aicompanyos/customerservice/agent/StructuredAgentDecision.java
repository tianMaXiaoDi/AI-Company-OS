package com.aicompanyos.customerservice.agent;

import java.util.Locale;
import java.util.Objects;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Future LLM adapters may produce only this constrained decision. They cannot
 * name tools, grant themselves authority, or issue executable business actions.
 */
public record StructuredAgentDecision(
        AgentIntent intent,
        @Pattern(regexp = "^ORD-[A-Z0-9-]+$") String orderId,
        @Size(max = 1000) String handoffReason) {

    public StructuredAgentDecision {
        Objects.requireNonNull(intent, "intent is required");
        orderId = normalizeOrderId(orderId);
        handoffReason = handoffReason == null ? null : handoffReason.trim();
    }

    private static String normalizeOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return null;
        }
        return orderId.trim().toUpperCase(Locale.ROOT);
    }
}
