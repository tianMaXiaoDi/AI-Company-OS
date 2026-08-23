package com.aicompanyos.customerservice.knowledge;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, UUID> {
    @EntityGraph(attributePaths = "document")
    List<KnowledgeChunk> findByDocumentStatus(KnowledgeDocumentStatus status);
}
