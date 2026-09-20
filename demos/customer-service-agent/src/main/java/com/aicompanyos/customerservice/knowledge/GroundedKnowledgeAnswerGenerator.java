package com.aicompanyos.customerservice.knowledge;

import java.util.Optional;

/**
 * Turns an already retrieved, citation-backed answer into a reader-friendly response.
 * Implementations must never introduce a citation that was not supplied by retrieval.
 */
public interface GroundedKnowledgeAnswerGenerator {
    Optional<KnowledgeAnswer> generate(String question, KnowledgeAnswer retrieved);
}
