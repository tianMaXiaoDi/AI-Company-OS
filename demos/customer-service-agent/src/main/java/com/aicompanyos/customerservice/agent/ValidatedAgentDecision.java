package com.aicompanyos.customerservice.agent;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Validates an LLM decision before it can reach the server-owned tool policy. */
final class ValidatedAgentDecision {
    private static final Pattern ORDER_ID = Pattern.compile("\\bORD-[A-Za-z0-9-]+\\b", Pattern.CASE_INSENSITIVE);
    private static final Set<String> FIELDS = Set.of("intent", "orderId", "handoffReason");

    private ValidatedAgentDecision() {
    }

    static Optional<StructuredAgentDecision> parse(ObjectMapper mapper, String modelContent, String customerMessage,
                                                   StructuredAgentDecision fallback) {
        try {
            JsonNode root = mapper.readTree(modelContent);
            if (!root.isObject() || root.size() != FIELDS.size() || !hasOnlyExpectedFields(root)) {
                return Optional.empty();
            }

            String intentName = text(root, "intent");
            String orderId = nullableText(root, "orderId");
            String modelHandoffReason = nullableText(root, "handoffReason");
            if (intentName == null || (root.get("orderId") != null && !root.get("orderId").isNull() && orderId == null)
                    || (root.get("handoffReason") != null && !root.get("handoffReason").isNull() && modelHandoffReason == null)
                    || (modelHandoffReason != null && modelHandoffReason.length() > 1_000)) {
                return Optional.empty();
            }

            AgentIntent intent = AgentIntent.valueOf(intentName);
            StructuredAgentDecision candidate = new StructuredAgentDecision(intent, orderId, null);
            if (!isOrderIdFromCustomerMessage(candidate.orderId(), customerMessage)
                    || !passesIntentSafetyChecks(candidate, customerMessage, fallback)) {
                return Optional.empty();
            }

            // Ticket text remains the original customer message, never model-authored text.
            String safeHandoffReason = candidate.intent() == AgentIntent.HUMAN_HANDOFF ? customerMessage : null;
            return Optional.of(new StructuredAgentDecision(candidate.intent(), candidate.orderId(), safeHandoffReason));
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    private static boolean hasOnlyExpectedFields(JsonNode root) {
        Set<String> names = new LinkedHashSet<>();
        root.fieldNames().forEachRemaining(names::add);
        return names.equals(FIELDS) && root.has("intent") && root.has("orderId") && root.has("handoffReason");
    }

    private static String text(JsonNode root, String field) {
        JsonNode value = root.get(field);
        return value != null && value.isTextual() && !value.asText().isBlank() ? value.asText().strip() : null;
    }

    private static String nullableText(JsonNode root, String field) {
        JsonNode value = root.get(field);
        return value == null || value.isNull() ? null : text(root, field);
    }

    private static boolean isOrderIdFromCustomerMessage(String candidateOrderId, String customerMessage) {
        if (candidateOrderId == null) {
            return true;
        }
        Matcher matcher = ORDER_ID.matcher(customerMessage);
        while (matcher.find()) {
            if (candidateOrderId.equals(matcher.group().toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static boolean passesIntentSafetyChecks(StructuredAgentDecision candidate, String message,
                                                    StructuredAgentDecision fallback) {
        if (fallback.intent() == AgentIntent.REFUND_REVIEW_REQUIRED
                && candidate.intent() != AgentIntent.REFUND_REVIEW_REQUIRED) {
            return false;
        }
        if (fallback.intent() == AgentIntent.HUMAN_HANDOFF && candidate.intent() != AgentIntent.HUMAN_HANDOFF) {
            return false;
        }
        if (candidate.intent() == AgentIntent.SHIPPING_STATUS && candidate.orderId() == null) {
            return false;
        }
        if (candidate.intent() == AgentIntent.HUMAN_HANDOFF && !hasExplicitHumanRequest(message)) {
            return false;
        }
        if ((candidate.intent() == AgentIntent.KNOWLEDGE_ANSWER || candidate.intent() == AgentIntent.UNSUPPORTED
                || candidate.intent() == AgentIntent.HUMAN_HANDOFF) && candidate.orderId() != null) {
            return false;
        }
        return true;
    }

    private static boolean hasExplicitHumanRequest(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("人工") || normalized.contains("客服") || normalized.contains("转接")
                || normalized.contains("human") || normalized.contains("representative")
                || normalized.contains("speak to") || normalized.contains("talk to");
    }
}
