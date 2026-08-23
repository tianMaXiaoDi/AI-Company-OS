package com.aicompanyos.customerservice.agent;

import java.util.List;

/**
 * The complete set of intents that an AI adapter may select in the current MVP.
 * Tool names are service-owned policy, not model-supplied values.
 */
public enum AgentIntent {
    SHIPPING_STATUS(AgentActionClass.AUTHORIZED_READ, List.of("getShippingStatus")),
    KNOWLEDGE_ANSWER(AgentActionClass.AUTHORIZED_READ, List.of("searchKnowledge")),
    REFUND_REVIEW_REQUIRED(AgentActionClass.HUMAN_APPROVAL_REQUIRED, List.of("getOrder")),
    HUMAN_HANDOFF(AgentActionClass.LOW_RISK_WRITE, List.of("createTicket")),
    UNSUPPORTED(AgentActionClass.NO_ACTION, List.of());

    private final AgentActionClass actionClass;
    private final List<String> allowedTools;

    AgentIntent(AgentActionClass actionClass, List<String> allowedTools) {
        this.actionClass = actionClass;
        this.allowedTools = List.copyOf(allowedTools);
    }

    public AgentActionClass actionClass() {
        return actionClass;
    }

    public List<String> allowedTools() {
        return allowedTools;
    }
}
