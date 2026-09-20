package com.aicompanyos.customerservice.agent;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/** Current provider-free decision engine used until an LLM adapter is explicitly enabled. */
@Service
public class DeterministicAgentDecisionEngine implements AgentDecisionEngine {
    private static final Pattern ORDER_ID = Pattern.compile("\\bORD-[A-Za-z0-9-]+\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public AgentDecisionResult decide(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        Optional<String> orderId = orderIdFrom(message);

        if (containsAny(normalized, "退款", "refund")
                && containsAny(normalized, "卡住", "进度", "状态", "没到账", "未到账", "为什么", "stuck", "status", "progress", "why")) {
            return result(AgentIntent.REFUND_STATUS_EXPLANATION, orderId.orElse(null), null);
        }
        if (containsAny(normalized, "退款", "refund")) {
            return result(AgentIntent.REFUND_REVIEW_REQUIRED, orderId.orElse(null), null);
        }
        if (containsAny(normalized, "人工", "客服", "human", "agent")) {
            return result(AgentIntent.HUMAN_HANDOFF, null, message);
        }
        if (orderId.isPresent() && containsAny(normalized, "物流", "到哪", "到哪里", "配送", "运送", "shipping", "deliver", "track")) {
            return result(AgentIntent.SHIPPING_STATUS, orderId.get(), null);
        }
        // Any remaining question may be answered only if the read-only retriever can find a verified source.
        // The agent converts a retrieval miss back to UNSUPPORTED and never invents a policy answer.
        return result(AgentIntent.KNOWLEDGE_ANSWER, null, null);
    }

    private static AgentDecisionResult result(AgentIntent intent, String orderId, String handoffReason) {
        return new AgentDecisionResult(new StructuredAgentDecision(intent, orderId, handoffReason), DecisionSource.DETERMINISTIC);
    }

    private static Optional<String> orderIdFrom(String message) {
        Matcher matcher = ORDER_ID.matcher(message);
        return matcher.find() ? Optional.of(matcher.group().toUpperCase(Locale.ROOT)) : Optional.empty();
    }

    private static boolean containsAny(String message, String... markers) {
        for (String marker : markers) {
            if (message.contains(marker)) {
                return true;
            }
        }
        return false;
    }
}
