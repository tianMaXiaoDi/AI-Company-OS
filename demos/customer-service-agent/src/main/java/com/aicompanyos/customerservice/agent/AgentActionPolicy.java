package com.aicompanyos.customerservice.agent;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

/** Enforces the server-owned allow list for every agent response and future AI adapter. */
@Component
public class AgentActionPolicy {
    public List<String> allowedTools(AgentIntent intent) {
        return Objects.requireNonNull(intent, "intent is required").allowedTools();
    }

    public boolean requiresHumanApproval(AgentIntent intent) {
        return Objects.requireNonNull(intent, "intent is required").actionClass()
                == AgentActionClass.HUMAN_APPROVAL_REQUIRED;
    }

    public void assertToolsAllowed(AgentIntent intent, List<String> toolsCalled) {
        List<String> requested = List.copyOf(toolsCalled);
        if (!allowedTools(intent).containsAll(requested)) {
            throw new IllegalArgumentException("Intent " + intent + " attempted a tool outside its allow list");
        }
    }
}
