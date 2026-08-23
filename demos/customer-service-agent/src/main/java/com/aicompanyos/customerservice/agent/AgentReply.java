package com.aicompanyos.customerservice.agent;

import java.util.List;

import com.aicompanyos.customerservice.knowledge.KnowledgeCitation;

public record AgentReply(
        String traceId,
        String intent,
        List<String> toolsCalled,
        String response,
        String ticketId,
        String errorCode,
        List<KnowledgeCitation> citations) {
}
