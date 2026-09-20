package com.aicompanyos.customerservice.knowledge;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Keeps the citation-first response unchanged unless the opt-in LLM profile is active. */
@Service
@Profile("!llm")
public class PassThroughGroundedKnowledgeAnswerGenerator implements GroundedKnowledgeAnswerGenerator {
    @Override
    public Optional<KnowledgeAnswer> generate(String question, KnowledgeAnswer retrieved) {
        return Optional.of(retrieved);
    }
}
