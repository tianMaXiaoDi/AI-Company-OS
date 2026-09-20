package com.aicompanyos.customerservice.tool;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import com.aicompanyos.customerservice.refund.RefundStatus;
import org.springframework.stereotype.Component;

/**
 * Demo read-only Java tool for a tool-calling workflow.
 *
 * <p>This intentionally uses an in-memory fixture rather than a repository so
 * the agent demo is repeatable. A production adapter must preserve the same
 * contract while enforcing customer ownership before looking up an order.</p>
 */
@Component
public class RefundStatusTool {
    private static final Map<String, RefundStatusToolResult> DEMO_RESULTS = Map.of(
            "ORD-REFUND-PROCESSING", new RefundStatusToolResult(
                    "ORD-REFUND-PROCESSING", true, RefundStatus.PROCESSING,
                    new BigDecimal("88.50"), Instant.parse("2026-09-23T10:00:00Z")),
            "ORD-REFUND-COMPLETED", new RefundStatusToolResult(
                    "ORD-REFUND-COMPLETED", true, RefundStatus.COMPLETED,
                    new BigDecimal("129.00"), Instant.parse("2026-09-18T10:00:00Z")));

    /**
     * Tool schema: {@code {"orderId":"ORD-..."}}. Callers must request an
     * order ID from the customer before invoking this tool.
     */
    public RefundStatusToolResult getRefundStatus(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId is required before calling RefundStatusTool");
        }
        String normalizedOrderId = orderId.trim().toUpperCase(java.util.Locale.ROOT);
        return DEMO_RESULTS.getOrDefault(normalizedOrderId, RefundStatusToolResult.orderNotFound(normalizedOrderId));
    }
}
