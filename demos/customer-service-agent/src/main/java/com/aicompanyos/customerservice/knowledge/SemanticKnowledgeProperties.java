package com.aicompanyos.customerservice.knowledge;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "customer-service.knowledge.semantic")
public record SemanticKnowledgeProperties(
        String baseUrl,
        String apiKey,
        String model,
        int dimension,
        double maximumDistance,
        boolean indexOnStartup) {

    public SemanticKnowledgeProperties {
        baseUrl = baseUrl == null || baseUrl.isBlank() ? "http://localhost:8088" : baseUrl.strip();
        model = model == null || model.isBlank() ? "BAAI/bge-m3" : model.strip();
        dimension = dimension == 0 ? 1024 : dimension;
        maximumDistance = maximumDistance == 0 ? 0.45 : maximumDistance;
        if (dimension != 1024) {
            throw new IllegalArgumentException("The current pgvector index is fixed to the BAAI/bge-m3 1024 dimensions.");
        }
        if (maximumDistance < 0 || maximumDistance > 2) {
            throw new IllegalArgumentException("maximumDistance must be between 0 and 2 for cosine distance.");
        }
    }
}
