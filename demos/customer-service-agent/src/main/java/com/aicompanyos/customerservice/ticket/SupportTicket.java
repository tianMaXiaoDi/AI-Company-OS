package com.aicompanyos.customerservice.ticket;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {
    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 64)
    private String customerId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    protected SupportTicket() {
    }

    public SupportTicket(String id, String customerId, String reason) {
        this.id = id;
        this.customerId = customerId;
        this.reason = reason;
        this.status = "OPEN";
        this.createdAt = Instant.now();
    }

    public String id() { return id; }
    public String status() { return status; }
}
