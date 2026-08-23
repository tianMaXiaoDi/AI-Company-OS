package com.aicompanyos.customerservice.knowledge;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "knowledge_chunks")
public class KnowledgeChunk {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private KnowledgeDocument document;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "citation_anchor", nullable = false, length = 120)
    private String citationAnchor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    protected KnowledgeChunk() {
    }

    public KnowledgeChunk(UUID id, KnowledgeDocument document, int chunkIndex, String citationAnchor, String content) {
        this.id = id;
        this.document = document;
        this.chunkIndex = chunkIndex;
        this.citationAnchor = citationAnchor;
        this.content = content;
    }

    public KnowledgeDocument document() {
        return document;
    }

    public UUID id() {
        return id;
    }

    public String citationAnchor() {
        return citationAnchor;
    }

    public String content() {
        return content;
    }
}
