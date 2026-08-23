package com.aicompanyos.customerservice.agent;

/**
 * Converts a customer message into a constrained, non-executable decision.
 * A future Spring AI adapter will implement this port without receiving direct
 * access to business tools or repositories.
 */
public interface AgentDecisionEngine {
    StructuredAgentDecision decide(String message);
}
