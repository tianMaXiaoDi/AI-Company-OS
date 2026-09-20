package com.aicompanyos.customerservice.refund;

import java.util.List;

/** A customer-facing explanation grounded in the fact IDs supplied by the server. */
public record RefundStatusExplanation(String response, List<String> factIds) {
    public RefundStatusExplanation {
        factIds = List.copyOf(factIds);
    }
}
