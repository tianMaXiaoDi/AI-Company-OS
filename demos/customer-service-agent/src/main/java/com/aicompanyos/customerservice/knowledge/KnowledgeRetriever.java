package com.aicompanyos.customerservice.knowledge;

import java.util.Optional;

/** Read-only boundary for citation-backed knowledge retrieval. */
public interface KnowledgeRetriever {
    Optional<KnowledgeAnswer> retrieve(String question);
}
