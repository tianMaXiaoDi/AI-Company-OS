package com.aicompanyos.customerservice.refund;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.aicompanyos.customerservice.tool.RefundStatusSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Rejects any model explanation that does not cite only the server-provided refund facts. */
final class ValidatedRefundStatusExplanation {
    private ValidatedRefundStatusExplanation() {
    }

    static Optional<RefundStatusExplanation> parse(ObjectMapper mapper, String modelContent,
                                                   RefundStatusSnapshot refund) {
        try {
            JsonNode root = mapper.readTree(modelContent);
            String answer = root.path("answer").asText("").strip();
            JsonNode factIds = root.path("factIds");
            if (answer.isBlank() || !factIds.isArray() || factIds.isEmpty()) {
                return Optional.empty();
            }

            Map<String, String> allowedFacts = factsFor(refund);
            Set<String> selected = new LinkedHashSet<>();
            for (JsonNode factId : factIds) {
                if (!factId.isTextual() || !selected.add(factId.asText())
                        || !allowedFacts.containsKey(factId.asText())) {
                    return Optional.empty();
                }
            }
            return Optional.of(new RefundStatusExplanation(answer, List.copyOf(selected)));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    static Map<String, String> factsFor(RefundStatusSnapshot refund) {
        Map<String, String> facts = new LinkedHashMap<>();
        facts.put("F1", "退款状态: " + refund.status().customerLabel());
        facts.put("F2", "原因说明: " + refund.reasonDescription());
        facts.put("F3", "最近更新时间: " + refund.updatedAt());
        facts.put("F4", "下一步: " + refund.nextAction());
        return Map.copyOf(facts);
    }
}
