package com.aicompanyos.customerservice.agent;

/**
 * Converts a customer message into a constrained, non-executable decision.
 * An adapter never receives direct access to business tools or repositories.
 */
public interface AgentDecisionEngine {
    AgentDecisionResult decide(String message);
}
