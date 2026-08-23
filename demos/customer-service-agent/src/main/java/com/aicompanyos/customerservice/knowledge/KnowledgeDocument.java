package com.aicompanyos.customerservice.knowledge;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocument {
    @Id
    private UUID id;

    @Column(name = "source_key", nullable = false, unique = true, length = 120)
    private String sourceKey;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "source_uri", nullable = false, length = 500)
    private String sourceUri;

    @Column(name = "published_at", nullable = false)
    private LocalDate publishedAt;

    @Column(name = "verified_at", nullable = false)
    private LocalDate verifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private KnowledgeDocumentStatus status;

    protected KnowledgeDocument() {
    }

    public KnowledgeDocument(UUID id, String sourceKey, String title, String sourceUri,
                             LocalDate publishedAt, LocalDate verifiedAt, KnowledgeDocumentStatus status) {
        this.id = id;
        this.sourceKey = sourceKey;
        this.title = title;
        this.sourceUri = sourceUri;
        this.publishedAt = publishedAt;
        this.verifiedAt = verifiedAt;
        this.status = status;
    }

    public String sourceKey() {
        return sourceKey;
    }

    public String title() {
        return title;
    }

    public String sourceUri() {
        return sourceUri;
    }

    public LocalDate verifiedAt() {
        return verifiedAt;
    }

    public KnowledgeDocumentStatus status() {
        return status;
    }
}
