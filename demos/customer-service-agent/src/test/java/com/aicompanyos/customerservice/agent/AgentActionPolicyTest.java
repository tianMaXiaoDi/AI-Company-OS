package com.aicompanyos.customerservice.agent;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AgentActionPolicyTest {
    private final AgentActionPolicy policy = new AgentActionPolicy();

    @Test
    void rejectsToolsOutsideTheIntentAllowList() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                policy.assertToolsAllowed(AgentIntent.SHIPPING_STATUS, List.of("refund")));
    }

    @Test
    void keepsRefundInHumanApprovalClass() {
        assertThat(policy.requiresHumanApproval(AgentIntent.REFUND_REVIEW_REQUIRED)).isTrue();
        assertThat(policy.allowedTools(AgentIntent.REFUND_REVIEW_REQUIRED)).containsExactly("getOrder");
    }

    @Test
    void permitsOnlyTheReadOnlyRefundStatusToolForDiagnosis() {
        assertThat(policy.requiresHumanApproval(AgentIntent.REFUND_STATUS_EXPLANATION)).isFalse();
        assertThat(policy.allowedTools(AgentIntent.REFUND_STATUS_EXPLANATION)).containsExactly("getRefundStatus");
    }

    @Test
    void decisionNormalizesOrderIdsAndDoesNotAcceptTools() {
        StructuredAgentDecision decision = new StructuredAgentDecision(
                AgentIntent.SHIPPING_STATUS, "ord-10086", null);

        assertThat(decision.orderId()).isEqualTo("ORD-10086");
        assertThat(decision.intent()).isEqualTo(AgentIntent.SHIPPING_STATUS);
    }
}
