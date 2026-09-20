package com.aicompanyos.customerservice.refund;

/** Customer-safe lifecycle states for a refund that has already been requested. */
public enum RefundStatus {
    PENDING_MANUAL_REVIEW("等待人工审核"),
    PROCESSING("退款处理中"),
    COMPLETED("退款已完成"),
    REJECTED("退款申请未通过");

    private final String customerLabel;

    RefundStatus(String customerLabel) {
        this.customerLabel = customerLabel;
    }

    public String customerLabel() {
        return customerLabel;
    }
}
