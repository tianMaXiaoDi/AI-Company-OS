package com.aicompanyos.customerservice.agent;

import org.springframework.http.HttpStatus;

public record AgentResult(HttpStatus status, AgentReply reply) {
    public static AgentResult ok(AgentReply reply) {
        return new AgentResult(HttpStatus.OK, reply);
    }

    public static AgentResult error(HttpStatus status, AgentReply reply) {
        return new AgentResult(status, reply);
    }
}
