package com.aicompanyos.customerservice.knowledge;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Opt-in local Ollama adapter. It sends only the customer question and already published
 * knowledge text to a loopback Ollama endpoint, then validates every returned citation.
 */
@Service
@Profile("legacy-llm")
public class LocalOllamaGroundedKnowledgeAnswerGenerator implements GroundedKnowledgeAnswerGenerator {
    private static final Logger log = LoggerFactory.getLogger(LocalOllamaGroundedKnowledgeAnswerGenerator.class);
    private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "answer", Map.of("type", "string"),
                    "citations", Map.of("type", "array", "items", Map.of("type", "string"))),
            "required", List.of("answer", "citations"),
            "additionalProperties", false);
    private static final String SYSTEM_PROMPT = """
            You are a customer-service knowledge assistant. Answer only from the supplied Sources.
            Do not use outside knowledge, make promises, infer policy details, or mention sources that are not supplied.
            Write a concise Chinese answer. The citations array must contain one or more supplied source IDs.
            If the sources do not answer the question, state that the verified source is insufficient and still cite the closest source.
            """;

    private final RestClient client;
    private final OllamaChatProperties properties;
    private final ObjectMapper mapper;

    public LocalOllamaGroundedKnowledgeAnswerGenerator(RestClient.Builder builder, OllamaChatProperties properties,
                                                       ObjectMapper mapper) {
        URI endpoint = URI.create(properties.baseUrl());
        String host = endpoint.getHost();
        if (host == null || !(host.equals("localhost") || host.equals("127.0.0.1") || host.equals("::1"))) {
            throw new IllegalArgumentException("The local LLM endpoint must be a loopback address. Remote providers are not approved yet.");
        }
        this.client = builder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    public Optional<KnowledgeAnswer> generate(String question, KnowledgeAnswer retrieved) {
        try {
            JsonNode response = client.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", properties.model(),
                            "stream", false,
                            "think", false,
                            "format", RESPONSE_SCHEMA,
                            "options", Map.of(
                                    "temperature", properties.temperature(),
                                    "num_predict", properties.maximumTokens()),
                            "messages", List.of(
                                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                                    Map.of("role", "user", "content", userPrompt(question, retrieved)))))
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || !response.path("message").path("content").isTextual()) {
                return Optional.empty();
            }
            return CitationValidatedKnowledgeAnswer.parse(mapper, response.path("message").path("content").asText(), retrieved);
        } catch (RestClientException exception) {
            log.warn("Local Ollama generation is unavailable; returning the verified retrieved source text.");
            return Optional.empty();
        }
    }

    private static String userPrompt(String question, KnowledgeAnswer retrieved) {
        Map<String, KnowledgeCitation> citationIds = CitationValidatedKnowledgeAnswer.citationIdsFor(retrieved.citations());
        String sources = citationIds.entrySet().stream()
                .map(entry -> "%s\nTitle: %s\nURI: %s\nVerified: %s\nContent:\n%s".formatted(
                        entry.getKey(), entry.getValue().title(), entry.getValue().sourceUri(),
                        entry.getValue().verifiedAt(), retrieved.response()))
                .collect(Collectors.joining("\n\n"));
        return "Question:\n%s\n\nSources:\n%s".formatted(question, sources);
    }
}
