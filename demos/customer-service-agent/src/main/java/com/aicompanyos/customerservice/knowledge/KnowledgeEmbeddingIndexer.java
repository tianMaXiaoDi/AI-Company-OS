package com.aicompanyos.customerservice.knowledge;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Re-indexes published, non-sensitive knowledge only when explicitly enabled by configuration. */
@Service
@Profile("semantic")
public class KnowledgeEmbeddingIndexer {
    private final KnowledgeChunkRepository chunks;
    private final EmbeddingClient embeddings;
    private final PgVectorKnowledgeSearch search;
    private final SemanticKnowledgeProperties properties;

    public KnowledgeEmbeddingIndexer(KnowledgeChunkRepository chunks, EmbeddingClient embeddings,
                                     PgVectorKnowledgeSearch search, SemanticKnowledgeProperties properties) {
        this.chunks = chunks;
        this.embeddings = embeddings;
        this.search = search;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void indexOnStartupWhenExplicitlyEnabled() {
        if (!properties.indexOnStartup()) {
            return;
        }
        List<KnowledgeChunk> published = chunks.findByDocumentStatus(KnowledgeDocumentStatus.PUBLISHED);
        List<EmbeddingVector> vectors = embeddings.embed(published.stream().map(KnowledgeChunk::content).toList());
        if (vectors.size() != published.size()) {
            throw new EmbeddingUnavailableException("The local embedding endpoint did not return every published knowledge vector.");
        }
        for (int index = 0; index < published.size(); index++) {
            KnowledgeChunk chunk = published.get(index);
            search.upsert(new PgVectorKnowledgeSearch.KnowledgeEmbedding(
                    chunk.id(), properties.model(), vectors.get(index)));
        }
    }
}
