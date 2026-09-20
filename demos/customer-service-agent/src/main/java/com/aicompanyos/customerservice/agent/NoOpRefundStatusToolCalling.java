package com.aicompanyos.customerservice.agent;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Uses the existing deterministic refund path unless the explicit llm profile is active. */
@Service
@Profile("!llm")
class NoOpRefundStatusToolCalling implements RefundStatusToolCalling {
    @Override
    public Optional<String> reply(String customerId, String orderId, String customerMessage) {
        return Optional.empty();
    }
}
