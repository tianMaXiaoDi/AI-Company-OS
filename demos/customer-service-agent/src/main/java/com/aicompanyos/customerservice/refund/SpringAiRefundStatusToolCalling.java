package com.aicompanyos.customerservice.refund;

import java.util.Optional;

import com.aicompanyos.customerservice.agent.RefundStatusToolCalling;
import com.aicompanyos.customerservice.tool.CustomerSupportTools;
import com.aicompanyos.customerservice.tool.RefundStatusSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Spring AI tool-calling adapter. The model can request the tool with an
 * order ID, but the authenticated customer and the allowed order reference are
 * server-owned invocation context, never model-supplied arguments.
 */
@Service
@Profile("llm")
public class SpringAiRefundStatusToolCalling implements RefundStatusToolCalling {
    private static final Logger log = LoggerFactory.getLogger(SpringAiRefundStatusToolCalling.class);
    private static final String SYSTEM_PROMPT = """
            You are a customer-service assistant handling an existing refund request.
            You must call getRefundStatus before answering. Use only the tool result as facts.
            Do not invent processing times, policies, commitments, or other facts.
            Return JSON only: {"answer":"concise Chinese answer","factIds":["F1"]}.
            factIds must cite one or more Server Fact IDs included in the tool result.
            """;

    private final ChatClient chatClient;
    private final CustomerSupportTools tools;
    private final ObjectMapper mapper;
    private final ThreadLocal<Invocation> invocation = new ThreadLocal<>();

    public SpringAiRefundStatusToolCalling(ChatClient.Builder builder, CustomerSupportTools tools, ObjectMapper mapper) {
        this.chatClient = builder.build();
        this.tools = tools;
        this.mapper = mapper;
    }

    @Override
    public Optional<String> reply(String customerId, String orderId, String customerMessage) {
        Invocation current = new Invocation(customerId, orderId);
        invocation.set(current);
        try {
            String modelContent = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(customerMessage)
                    .tools(this)
                    .call()
                    .content();
            if (current.refund == null || modelContent == null) {
                return Optional.empty();
            }
            return ValidatedRefundStatusExplanation.parse(mapper, modelContent, current.refund)
                    .map(RefundStatusExplanation::response);
        } catch (RuntimeException exception) {
            log.warn("Spring AI refund tool calling was unavailable or rejected; using deterministic fallback.");
            return Optional.empty();
        } finally {
            invocation.remove();
        }
    }

    @Tool(description = "Get the authenticated customer's refund status for the order ID in the current request.")
    public RefundStatusSnapshot getRefundStatus(String orderId) {
        Invocation current = invocation.get();
        if (current == null) {
            throw new IllegalStateException("Refund tool may be called only during an authorized agent request");
        }
        if (orderId == null || !current.orderId.equalsIgnoreCase(orderId.strip())) {
            throw new IllegalArgumentException("The tool may query only the order ID supplied by the customer");
        }
        RefundStatusSnapshot refund = tools.getRefundStatus(current.customerId, current.orderId);
        current.refund = refund;
        return refund;
    }

    private static final class Invocation {
        private final String customerId;
        private final String orderId;
        private RefundStatusSnapshot refund;

        private Invocation(String customerId, String orderId) {
            this.customerId = customerId;
            this.orderId = orderId;
        }
    }
}
