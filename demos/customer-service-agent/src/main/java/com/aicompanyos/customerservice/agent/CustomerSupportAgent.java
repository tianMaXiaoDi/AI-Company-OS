package com.aicompanyos.customerservice.agent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.aicompanyos.customerservice.audit.AuditService;
import com.aicompanyos.customerservice.knowledge.KnowledgeAnswer;
import com.aicompanyos.customerservice.knowledge.GroundedKnowledgeAnswerGenerator;
import com.aicompanyos.customerservice.knowledge.KnowledgeRetriever;
import com.aicompanyos.customerservice.order.OrderNotAvailableException;
import com.aicompanyos.customerservice.order.OrderNotFoundException;
import com.aicompanyos.customerservice.refund.RefundNotFoundException;
import com.aicompanyos.customerservice.refund.RefundStatusExplanation;
import com.aicompanyos.customerservice.refund.RefundStatusExplanationGenerator;
import com.aicompanyos.customerservice.tool.CustomerSupportTools;
import com.aicompanyos.customerservice.tool.OrderSnapshot;
import com.aicompanyos.customerservice.tool.RefundStatusSnapshot;
import com.aicompanyos.customerservice.tool.TicketSnapshot;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * v0.2 deterministic agent router. Future LLM structured output may select an intent,
 * but all business facts and all mutations remain behind CustomerSupportTools.
 */
@Service
public class CustomerSupportAgent {
    private final CustomerSupportTools tools;
    private final RedisConversationMemory memory;
    private final AuditService audit;
    private final AgentActionPolicy actionPolicy;
    private final AgentDecisionEngine decisionEngine;
    private final KnowledgeRetriever knowledgeRetriever;
    private final GroundedKnowledgeAnswerGenerator answerGenerator;
    private final RefundStatusExplanationGenerator refundExplanationGenerator;

    public CustomerSupportAgent(CustomerSupportTools tools, RedisConversationMemory memory, AuditService audit,
                                AgentActionPolicy actionPolicy, AgentDecisionEngine decisionEngine,
                                KnowledgeRetriever knowledgeRetriever, GroundedKnowledgeAnswerGenerator answerGenerator,
                                RefundStatusExplanationGenerator refundExplanationGenerator) {
        this.tools = tools;
        this.memory = memory;
        this.audit = audit;
        this.actionPolicy = actionPolicy;
        this.decisionEngine = decisionEngine;
        this.knowledgeRetriever = knowledgeRetriever;
        this.answerGenerator = answerGenerator;
        this.refundExplanationGenerator = refundExplanationGenerator;
    }

    public AgentResult respond(String customerId, String sessionId, String message) {
        String traceId = UUID.randomUUID().toString();
        AgentDecisionResult decisionResult = decisionEngine.decide(message);
        StructuredAgentDecision decision = decisionResult.decision();
        audit.record(traceId, sessionId, customerId, "agent.decision", "classifyIntent", decisionResult.source().name());

        if (decision.intent() == AgentIntent.REFUND_STATUS_EXPLANATION) {
            Optional<String> orderId = Optional.ofNullable(decision.orderId()).or(() -> memory.lastOrder(customerId, sessionId));
            if (orderId.isEmpty()) {
                audit.record(traceId, sessionId, customerId, "refund.status.explanation", "none", "REFERENCE_REQUIRED");
                return AgentResult.ok(reply(traceId, AgentIntent.REFUND_STATUS_EXPLANATION, List.of(),
                        "请提供订单号，或先查询对应订单后再询问退款状态。", null, "REFUND_REFERENCE_REQUIRED"));
            }
            try {
                RefundStatusSnapshot refund = tools.getRefundStatus(customerId, orderId.get());
                memory.rememberOrder(customerId, sessionId, orderId.get());
                Optional<RefundStatusExplanation> explanation = refundExplanationGenerator.generate(message, refund);
                String outcome = explanation.isPresent() ? "LLM_GROUNDED" : "DETERMINISTIC_FALLBACK";
                String response = explanation.map(RefundStatusExplanation::response).orElseGet(() -> refundStatusReply(refund));
                audit.record(traceId, sessionId, customerId, "refund.status.explanation", "getRefundStatus", outcome);
                return AgentResult.ok(reply(traceId, AgentIntent.REFUND_STATUS_EXPLANATION, List.of("getRefundStatus"),
                        response, null, null));
            } catch (OrderNotAvailableException exception) {
                return denied(traceId, sessionId, customerId, AgentIntent.REFUND_STATUS_EXPLANATION,
                        "refund.status.explanation", "getRefundStatus");
            } catch (OrderNotFoundException exception) {
                return missing(traceId, sessionId, customerId, AgentIntent.REFUND_STATUS_EXPLANATION,
                        "refund.status.explanation", "getRefundStatus");
            } catch (RefundNotFoundException exception) {
                audit.record(traceId, sessionId, customerId, "refund.status.explanation", "getRefundStatus", "NOT_FOUND");
                return AgentResult.error(HttpStatus.NOT_FOUND, errorReply(traceId, AgentIntent.REFUND_STATUS_EXPLANATION,
                        List.of("getRefundStatus"), "未找到该订单对应的退款申请。", "REFUND_NOT_FOUND"));
            }
        }

        if (decision.intent() == AgentIntent.REFUND_REVIEW_REQUIRED) {
            Optional<String> orderId = Optional.ofNullable(decision.orderId()).or(() -> memory.lastOrder(customerId, sessionId));
            try {
                orderId.ifPresent(id -> tools.getOrder(customerId, id));
                audit.record(traceId, sessionId, customerId, "refund.review", orderId.isPresent() ? "getOrder" : "none", "HUMAN_REVIEW_REQUIRED");
                String suffix = orderId.map(id -> "订单 " + id + " 的").orElse("");
                return AgentResult.ok(reply(traceId, AgentIntent.REFUND_REVIEW_REQUIRED, orderId.isPresent() ? List.of("getOrder") : List.of(),
                        suffix + "退款申请必须经过人工审核；当前不会自动执行退款。", null, null));
            } catch (OrderNotAvailableException exception) {
                return denied(traceId, sessionId, customerId, AgentIntent.REFUND_REVIEW_REQUIRED, "refund.review", "getOrder");
            } catch (OrderNotFoundException exception) {
                return missing(traceId, sessionId, customerId, AgentIntent.REFUND_REVIEW_REQUIRED, "refund.review", "getOrder");
            }
        }

        if (decision.intent() == AgentIntent.HUMAN_HANDOFF) {
            TicketSnapshot ticket = tools.createTicket(customerId, decision.handoffReason());
            audit.record(traceId, sessionId, customerId, "human.handoff", "createTicket", "SUCCEEDED");
            return AgentResult.ok(reply(traceId, AgentIntent.HUMAN_HANDOFF, List.of("createTicket"),
                    "已创建人工支持工单 " + ticket.id() + "，客服会跟进。", ticket.id(), null));
        }

        if (decision.intent() == AgentIntent.SHIPPING_STATUS && decision.orderId() != null) {
            try {
                OrderSnapshot order = tools.getOrder(customerId, decision.orderId());
                memory.rememberOrder(customerId, sessionId, order.id());
                audit.record(traceId, sessionId, customerId, "shipping.status", "getShippingStatus", "SUCCEEDED");
                return AgentResult.ok(reply(traceId, AgentIntent.SHIPPING_STATUS, List.of("getShippingStatus"), order.shippingReply(), null, null));
            } catch (OrderNotAvailableException exception) {
                return denied(traceId, sessionId, customerId, AgentIntent.SHIPPING_STATUS, "shipping.status", "getShippingStatus");
            } catch (OrderNotFoundException exception) {
                return missing(traceId, sessionId, customerId, AgentIntent.SHIPPING_STATUS, "shipping.status", "getShippingStatus");
            }
        }

        if (decision.intent() == AgentIntent.KNOWLEDGE_ANSWER) {
            Optional<KnowledgeAnswer> answer = knowledgeRetriever.retrieve(message);
            if (answer.isPresent()) {
                audit.record(traceId, sessionId, customerId, "knowledge.answer", "searchKnowledge", "SUCCEEDED");
                KnowledgeAnswer result = answerGenerator.generate(message, answer.get()).orElse(answer.get());
                return AgentResult.ok(reply(traceId, AgentIntent.KNOWLEDGE_ANSWER, List.of("searchKnowledge"),
                        result.response(), null, null, result.citations()));
            }
            audit.record(traceId, sessionId, customerId, "knowledge.answer", "searchKnowledge", "NO_VERIFIED_SOURCE");
            return AgentResult.ok(reply(traceId, AgentIntent.UNSUPPORTED, List.of(),
                    "暂未找到可核验的政策来源。请转人工客服确认。", null, "KNOWLEDGE_SOURCE_NOT_FOUND", List.of()));
        }

        audit.record(traceId, sessionId, customerId, "chat.unsupported", "none", "REQUIRES_KNOWLEDGE_OR_HUMAN");
        return AgentResult.ok(reply(traceId, AgentIntent.UNSUPPORTED, List.of(),
                "当前版本可查询订单物流或转人工。政策类问题将在企业知识库接入后提供带来源的回答。", null, null, List.of()));
    }

    private AgentResult denied(String traceId, String sessionId, String customerId, AgentIntent intent, String workflow, String tool) {
        audit.record(traceId, sessionId, customerId, workflow, tool, "DENIED");
        return AgentResult.error(HttpStatus.FORBIDDEN, errorReply(traceId, intent, List.of(tool), "该订单不属于当前客户，无法查询。", "ORDER_NOT_AVAILABLE"));
    }

    private AgentResult missing(String traceId, String sessionId, String customerId, AgentIntent intent, String workflow, String tool) {
        audit.record(traceId, sessionId, customerId, workflow, tool, "NOT_FOUND");
        return AgentResult.error(HttpStatus.NOT_FOUND, errorReply(traceId, intent, List.of(tool), "未找到该订单。", "ORDER_NOT_FOUND"));
    }

    private AgentReply reply(String traceId, AgentIntent intent, List<String> toolsCalled, String message, String ticketId, String errorCode) {
        actionPolicy.assertToolsAllowed(intent, toolsCalled);
        return new AgentReply(traceId, intent.name(), toolsCalled, message, ticketId, errorCode, List.of());
    }

    private AgentReply errorReply(String traceId, AgentIntent intent, List<String> toolsCalled, String message, String errorCode) {
        actionPolicy.assertToolsAllowed(intent, toolsCalled);
        return new AgentReply(traceId, "ERROR", toolsCalled, message, null, errorCode, List.of());
    }

    private AgentReply reply(String traceId, AgentIntent intent, List<String> toolsCalled, String message,
                             String ticketId, String errorCode, List<com.aicompanyos.customerservice.knowledge.KnowledgeCitation> citations) {
        actionPolicy.assertToolsAllowed(intent, toolsCalled);
        return new AgentReply(traceId, intent.name(), toolsCalled, message, ticketId, errorCode, List.copyOf(citations));
    }

    private static String refundStatusReply(RefundStatusSnapshot refund) {
        return "退款单 %s 当前处于%s。原因：%s。最近更新时间：%s。下一步：%s。".formatted(
                refund.refundId(), refund.status().customerLabel(), refund.reasonDescription(),
                refund.updatedAt(), refund.nextAction());
    }

}
