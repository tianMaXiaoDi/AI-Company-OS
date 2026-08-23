package com.aicompanyos.customerservice.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "agent_audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 64)
    private String traceId;

    @Column(nullable = false, length = 128)
    private String sessionId;

    @Column(nullable = false, length = 64)
    private String customerId;

    @Column(nullable = false, length = 80)
    private String workflow;

    @Column(nullable = false, length = 80)
    private String toolName;

    @Column(nullable = false, length = 80)
    private String outcome;

    protected AuditEvent() {
    }

    public AuditEvent(String traceId, String sessionId, String customerId, String workflow, String toolName, String outcome) {
        this.occurredAt = Instant.now();
        this.traceId = traceId;
        this.sessionId = sessionId;
        this.customerId = customerId;
        this.workflow = workflow;
        this.toolName = toolName;
        this.outcome = outcome;
    }

    public String traceId() { return traceId; }
    public String workflow() { return workflow; }
    public String toolName() { return toolName; }
    public String outcome() { return outcome; }
}
