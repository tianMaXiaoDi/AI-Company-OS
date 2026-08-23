package com.aicompanyos.customerservice.agent;

/**
 * The maximum authority associated with an agent intent. The agent may never
 * elevate an intent to a more powerful action class at runtime.
 */
public enum AgentActionClass {
    NO_ACTION,
    AUTHORIZED_READ,
    LOW_RISK_WRITE,
    HUMAN_APPROVAL_REQUIRED
}
