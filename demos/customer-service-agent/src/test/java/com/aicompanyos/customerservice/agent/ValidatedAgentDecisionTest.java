package com.aicompanyos.customerservice.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatedAgentDecisionTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void acceptsAShippingDecisionForAnOrderActuallyNamedByTheCustomer() {
        var result = ValidatedAgentDecision.parse(mapper, """
                {"intent":"SHIPPING_STATUS","orderId":"ord-10086","handoffReason":null}
                """, "Where is order ORD-10086?", knowledgeFallback());

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().intent()).isEqualTo(AgentIntent.SHIPPING_STATUS);
        assertThat(result.orElseThrow().orderId()).isEqualTo("ORD-10086");
    }

    @Test
    void rejectsAnOrderIdInventedByTheModel() {
        var result = ValidatedAgentDecision.parse(mapper,
                "{\"intent\":\"SHIPPING_STATUS\",\"orderId\":\"ORD-99999\",\"handoffReason\":null}",
                "Where is my order?", knowledgeFallback());

        assertThat(result).isEmpty();
    }

    @Test
    void cannotDowngradeAnExplicitRefundRequestToAReadOnlyStatusLookup() {
        StructuredAgentDecision refundFallback = new StructuredAgentDecision(AgentIntent.REFUND_REVIEW_REQUIRED, "ORD-10086", null);
        var result = ValidatedAgentDecision.parse(mapper,
                "{\"intent\":\"REFUND_STATUS_EXPLANATION\",\"orderId\":\"ORD-10086\",\"handoffReason\":null}",
                "Please refund ORD-10086 now", refundFallback);

        assertThat(result).isEmpty();
    }

    @Test
    void usesTheOriginalMessageForAValidatedHumanHandoff() {
        String message = "Please transfer me to a human representative.";
        var result = ValidatedAgentDecision.parse(mapper,
                "{\"intent\":\"HUMAN_HANDOFF\",\"orderId\":null,\"handoffReason\":\"different text\"}",
                message, knowledgeFallback());

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().handoffReason()).isEqualTo(message);
    }

    private static StructuredAgentDecision knowledgeFallback() {
        return new StructuredAgentDecision(AgentIntent.KNOWLEDGE_ANSWER, null, null);
    }
}
