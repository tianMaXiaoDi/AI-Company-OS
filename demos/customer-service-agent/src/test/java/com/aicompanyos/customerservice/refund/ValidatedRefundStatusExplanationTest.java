package com.aicompanyos.customerservice.refund;

import java.time.Instant;

import com.aicompanyos.customerservice.tool.RefundStatusSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatedRefundStatusExplanationTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final RefundStatusSnapshot refund = new RefundStatusSnapshot("RF-10001", "ORD-10086",
            RefundStatus.PENDING_MANUAL_REVIEW, "PAYMENT_RISK_REVIEW", "Manual review is required.",
            "WAIT_FOR_OPERATIONS_REVIEW", Instant.parse("2026-08-24T12:30:00Z"));

    @Test
    void acceptsAnAnswerCitingOnlyKnownServerFactIds() {
        var result = ValidatedRefundStatusExplanation.parse(mapper, """
                {"answer":"The refund is waiting for a manual review.","factIds":["F1","F2"]}
                """, refund);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().factIds()).containsExactly("F1", "F2");
    }

    @Test
    void rejectsUnknownDuplicateOrMissingFactIds() {
        assertThat(ValidatedRefundStatusExplanation.parse(mapper,
                "{\"answer\":\"answer\",\"factIds\":[\"F1\",\"F5\"]}", refund)).isEmpty();
        assertThat(ValidatedRefundStatusExplanation.parse(mapper,
                "{\"answer\":\"answer\",\"factIds\":[\"F1\",\"F1\"]}", refund)).isEmpty();
        assertThat(ValidatedRefundStatusExplanation.parse(mapper,
                "{\"answer\":\"answer\",\"factIds\":[]}", refund)).isEmpty();
    }
}
