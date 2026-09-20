package com.aicompanyos.customerservice.tool;

import java.math.BigDecimal;
import java.time.Instant;

import com.aicompanyos.customerservice.refund.RefundStatus;

/**
 * Customer-safe result returned by {@link RefundStatusTool}. The values are
 * tool facts: an LLM must not create, alter, or infer them.
 */
public record RefundStatusToolResult(
        String orderId,
        boolean orderFound,
        RefundStatus refundStatus,
        BigDecimal refundAmount,
        Instant expectedArrivalAt) {

    public static RefundStatusToolResult orderNotFound(String orderId) {
        return new RefundStatusToolResult(orderId, false, null, null, null);
    }
}
