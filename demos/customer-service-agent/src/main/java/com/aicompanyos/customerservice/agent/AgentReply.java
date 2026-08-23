package com.aicompanyos.customerservice.agent;

import java.util.List;

public record AgentReply(
        String traceId,
        String intent,
        List<String> toolsCalled,
        String response,
        String ticketId,
        String errorCode) {
}
