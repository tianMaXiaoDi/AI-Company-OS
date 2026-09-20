package com.aicompanyos.customerservice.refund;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Read model populated by the approved refund/operations workflow. The agent never writes it. */
@Entity
@Table(name = "refund_cases")
public class RefundCase {
    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private RefundStatus status;

    @Column(name = "reason_code", nullable = false, length = 80)
    private String reasonCode;

    @Column(name = "reason_description", nullable = false, length = 1000)
    private String reasonDescription;

    @Column(name = "next_action", nullable = false, length = 80)
    private String nextAction;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RefundCase() {
    }

    public RefundCase(String id, String orderId, RefundStatus status, String reasonCode,
                      String reasonDescription, String nextAction, Instant updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.reasonCode = reasonCode;
        this.reasonDescription = reasonDescription;
        this.nextAction = nextAction;
        this.updatedAt = updatedAt;
    }

    public String id() { return id; }
    public String orderId() { return orderId; }
    public RefundStatus status() { return status; }
    public String reasonCode() { return reasonCode; }
    public String reasonDescription() { return reasonDescription; }
    public String nextAction() { return nextAction; }
    public Instant updatedAt() { return updatedAt; }
}
