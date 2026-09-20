package com.aicompanyos.customerservice.refund;

import java.util.Optional;

import com.aicompanyos.customerservice.tool.RefundStatusSnapshot;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Keeps the deterministic explanation path when the opt-in LLM profile is not active. */
@Service
@Profile("!legacy-llm")
public class NoOpRefundStatusExplanationGenerator implements RefundStatusExplanationGenerator {
    @Override
    public Optional<RefundStatusExplanation> generate(String question, RefundStatusSnapshot refund) {
        return Optional.empty();
    }
}
