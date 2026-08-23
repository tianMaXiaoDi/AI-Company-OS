package com.aicompanyos.customerservice.knowledge;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Calls a loopback-only OpenAI-compatible embedding endpoint. The semantic profile is opt-in;
 * default application startup never invokes this client or transmits a customer question.
 */
@Service
@Profile("semantic")
public class LocalOpenAiCompatibleEmbeddingClient implements EmbeddingClient {
    private final RestClient client;
    private final SemanticKnowledgeProperties properties;

    public LocalOpenAiCompatibleEmbeddingClient(RestClient.Builder builder, SemanticKnowledgeProperties properties) {
        URI endpoint = URI.create(properties.baseUrl());
        String host = endpoint.getHost();
        if (host == null || !(host.equals("localhost") || host.equals("127.0.0.1") || host.equals("::1"))) {
            throw new IllegalArgumentException("The semantic embedding endpoint must be local. Remote embedding is not approved yet.");
        }
        this.client = builder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    @Override
    public List<EmbeddingVector> embed(List<String> inputs) {
        if (inputs.isEmpty()) {
            return List.of();
        }
        try {
            JsonNode response = client.post()
                    .uri("/v1/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        if (properties.apiKey() != null && !properties.apiKey().isBlank()) {
                            headers.setBearerAuth(properties.apiKey());
                        }
                    })
                    .body(Map.of("model", properties.model(), "input", inputs))
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || !response.path("data").isArray() || response.path("data").size() != inputs.size()) {
                throw new EmbeddingUnavailableException("The local embedding endpoint returned an invalid response.");
            }
            List<EmbeddingVector> vectors = new ArrayList<>();
            for (JsonNode item : response.path("data")) {
                List<Double> values = new ArrayList<>();
                for (JsonNode value : item.path("embedding")) {
                    values.add(value.doubleValue());
                }
                EmbeddingVector vector = new EmbeddingVector(values);
                assertDimension(vector);
                vectors.add(vector);
            }
            return List.copyOf(vectors);
        } catch (RestClientException exception) {
            throw new EmbeddingUnavailableException("The local embedding endpoint is unavailable.", exception);
        }
    }

    private void assertDimension(EmbeddingVector vector) {
        if (vector.dimension() != properties.dimension()) {
            throw new EmbeddingUnavailableException("The embedding endpoint returned an unexpected vector dimension.");
        }
    }
}
