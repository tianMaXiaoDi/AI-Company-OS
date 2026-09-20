package com.aicompanyos.customerservice.knowledge;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Validates that an LLM response can cite only sources the retriever supplied. */
final class CitationValidatedKnowledgeAnswer {
    private CitationValidatedKnowledgeAnswer() {
    }

    static Optional<KnowledgeAnswer> parse(ObjectMapper mapper, String modelContent, KnowledgeAnswer retrieved) {
        try {
            JsonNode root = mapper.readTree(modelContent);
            String answer = root.path("answer").asText("").strip();
            JsonNode citationIds = root.path("citations");
            if (answer.isBlank() || !citationIds.isArray() || citationIds.isEmpty()) {
                return Optional.empty();
            }

            Map<String, KnowledgeCitation> allowedCitations = citationIdsFor(retrieved.citations());
            Set<String> requestedIds = new LinkedHashSet<>();
            for (JsonNode citationId : citationIds) {
                if (!citationId.isTextual() || !requestedIds.add(citationId.asText())
                        || !allowedCitations.containsKey(citationId.asText())) {
                    return Optional.empty();
                }
            }
            return Optional.of(new KnowledgeAnswer(answer, requestedIds.stream()
                    .map(allowedCitations::get)
                    .toList()));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    static Map<String, KnowledgeCitation> citationIdsFor(List<KnowledgeCitation> citations) {
        Map<String, KnowledgeCitation> result = new LinkedHashMap<>();
        for (int index = 0; index < citations.size(); index++) {
            result.put("S" + (index + 1), citations.get(index));
        }
        return Map.copyOf(result);
    }
}
