package com.aicompanyos.customerservice.tool;

import java.time.Instant;

import com.aicompanyos.customerservice.refund.RefundStatus;

/** Only customer-safe, read-only facts that an agent may use to explain a refund status. */
public record RefundStatusSnapshot(
        String refundId,
        String orderId,
        RefundStatus status,
        String reasonCode,
        String reasonDescription,
        String nextAction,
        Instant updatedAt) {
}
