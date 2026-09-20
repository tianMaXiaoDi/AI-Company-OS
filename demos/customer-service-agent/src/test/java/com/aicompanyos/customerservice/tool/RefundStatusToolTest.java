package com.aicompanyos.customerservice.tool;

import java.math.BigDecimal;
import java.time.Instant;

import com.aicompanyos.customerservice.refund.RefundStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundStatusToolTest {
    private final RefundStatusTool tool = new RefundStatusTool();

    @Test
    void returnsProcessingRefundFactsFromMockData() {
        RefundStatusToolResult result = tool.getRefundStatus("ord-refund-processing");

        assertThat(result.orderFound()).isTrue();
        assertThat(result.refundStatus()).isEqualTo(RefundStatus.PROCESSING);
        assertThat(result.refundAmount()).isEqualByComparingTo(new BigDecimal("88.50"));
        assertThat(result.expectedArrivalAt()).isEqualTo(Instant.parse("2026-09-23T10:00:00Z"));
    }

    @Test
    void returnsCompletedRefundFactsFromMockData() {
        RefundStatusToolResult result = tool.getRefundStatus("ORD-REFUND-COMPLETED");

        assertThat(result.orderFound()).isTrue();
        assertThat(result.refundStatus()).isEqualTo(RefundStatus.COMPLETED);
        assertThat(result.refundAmount()).isEqualByComparingTo(new BigDecimal("129.00"));
    }

    @Test
    void returnsExplicitNotFoundResultInsteadOfInventingRefundFacts() {
        RefundStatusToolResult result = tool.getRefundStatus("ORD-UNKNOWN");

        assertThat(result.orderFound()).isFalse();
        assertThat(result.refundStatus()).isNull();
        assertThat(result.refundAmount()).isNull();
        assertThat(result.expectedArrivalAt()).isNull();
    }

    @Test
    void rejectsMissingOrderIdBeforeLookingUpMockData() {
        assertThatThrownBy(() -> tool.getRefundStatus(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId is required");
    }
}
