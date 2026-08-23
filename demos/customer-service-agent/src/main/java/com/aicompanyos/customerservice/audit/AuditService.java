package com.aicompanyos.customerservice.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditEventRepository events;

    public AuditService(AuditEventRepository events) {
        this.events = events;
    }

    /** Writes an append-only event in its own transaction so failed user operations remain observable. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String traceId, String sessionId, String customerId, String workflow, String toolName, String outcome) {
        events.save(new AuditEvent(traceId, sessionId, customerId, workflow, toolName, outcome));
    }
}
