package com.aicompanyos.customerservice.refund;

import java.util.Optional;

import com.aicompanyos.customerservice.tool.RefundStatusSnapshot;

/** Converts an already-authorized refund snapshot into customer-friendly text. */
public interface RefundStatusExplanationGenerator {
    Optional<RefundStatusExplanation> generate(String question, RefundStatusSnapshot refund);
}
