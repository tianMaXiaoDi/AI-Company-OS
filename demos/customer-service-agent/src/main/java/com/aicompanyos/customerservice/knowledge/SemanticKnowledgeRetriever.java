package com.aicompanyos.customerservice.knowledge;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Opt-in semantic retrieval with a safe lexical fallback when the local model is unavailable. */
@Service
@Primary
@Profile("semantic")
public class SemanticKnowledgeRetriever implements KnowledgeRetriever {
    private static final Logger log = LoggerFactory.getLogger(SemanticKnowledgeRetriever.class);

    private final CuratedKnowledgeRetriever lexicalFallback;
    private final EmbeddingClient embeddings;
    private final PgVectorKnowledgeSearch semanticSearch;
    private final SemanticKnowledgeProperties properties;

    public SemanticKnowledgeRetriever(CuratedKnowledgeRetriever lexicalFallback, EmbeddingClient embeddings,
                                      PgVectorKnowledgeSearch semanticSearch, SemanticKnowledgeProperties properties) {
        this.lexicalFallback = lexicalFallback;
        this.embeddings = embeddings;
        this.semanticSearch = semanticSearch;
        this.properties = properties;
    }

    @Override
    public Optional<KnowledgeAnswer> retrieve(String question) {
        try {
            EmbeddingVector query = embeddings.embed(List.of(question)).getFirst();
            Optional<KnowledgeAnswer> semanticAnswer = semanticSearch.findNearest(query, properties.model(), properties.maximumDistance());
            return semanticAnswer.or(() -> lexicalFallback.retrieve(question));
        } catch (EmbeddingUnavailableException exception) {
            log.warn("Local semantic retrieval is unavailable; using the citation-first lexical fallback.");
            return lexicalFallback.retrieve(question);
        }
    }
}
