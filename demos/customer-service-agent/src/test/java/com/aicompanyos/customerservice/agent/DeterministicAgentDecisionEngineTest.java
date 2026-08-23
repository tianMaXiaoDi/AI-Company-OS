package com.aicompanyos.customerservice.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicAgentDecisionEngineTest {
    private final DeterministicAgentDecisionEngine engine = new DeterministicAgentDecisionEngine();

    @Test
    void producesAConstrainedShippingDecision() {
        StructuredAgentDecision decision = engine.decide("我的订单 ORD-10086 到哪里了？");

        assertThat(decision.intent()).isEqualTo(AgentIntent.SHIPPING_STATUS);
        assertThat(decision.orderId()).isEqualTo("ORD-10086");
        assertThat(decision.handoffReason()).isNull();
    }

    @Test
    void refundAlwaysStaysInReviewIntent() {
        StructuredAgentDecision decision = engine.decide("请直接给 ORD-10086 退款");

        assertThat(decision.intent()).isEqualTo(AgentIntent.REFUND_REVIEW_REQUIRED);
        assertThat(decision.orderId()).isEqualTo("ORD-10086");
    }
}
