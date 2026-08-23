package com.aicompanyos.customerservice.agent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.aicompanyos.customerservice.audit.AuditService;
import com.aicompanyos.customerservice.knowledge.KnowledgeAnswer;
import com.aicompanyos.customerservice.knowledge.KnowledgeCitation;
import com.aicompanyos.customerservice.knowledge.KnowledgeRetriever;
import com.aicompanyos.customerservice.order.OrderNotAvailableException;
import com.aicompanyos.customerservice.tool.CustomerSupportTools;
import com.aicompanyos.customerservice.tool.OrderSnapshot;
import com.aicompanyos.customerservice.tool.TicketSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerSupportAgentTest {
    @Mock private CustomerSupportTools tools;
    @Mock private RedisConversationMemory memory;
    @Mock private AuditService audit;
    @Mock private KnowledgeRetriever knowledge;

    private CustomerSupportAgent agent;

    @BeforeEach
    void setUp() {
        agent = new CustomerSupportAgent(tools, memory, audit, new AgentActionPolicy(), new DeterministicAgentDecisionEngine(), knowledge);
    }

    @Test
    void returnsShippingStatusFromTrustedTool() {
        when(tools.getOrder("CUST-1001", "ORD-10086"))
                .thenReturn(new OrderSnapshot("ORD-10086", "MacBook Case", "SHIPPED", "DHL", "DHL123456", "2026-09-02"));

        AgentResult result = agent.respond("CUST-1001", "session-1", "我的订单 ORD-10086 到哪里了？");

        assertThat(result.status().value()).isEqualTo(200);
        assertThat(result.reply().intent()).isEqualTo("SHIPPING_STATUS");
        assertThat(result.reply().toolsCalled()).isEqualTo(List.of("getShippingStatus"));
        assertThat(result.reply().response()).contains("DHL");
        verify(memory).rememberOrder("CUST-1001", "session-1", "ORD-10086");
        verify(audit).record(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void deniesCrossCustomerOrderWithoutLeakingDetails() {
        when(tools.getOrder("CUST-1001", "ORD-20001")).thenThrow(new OrderNotAvailableException());

        AgentResult result = agent.respond("CUST-1001", "session-1", "查询 ORD-20001 的物流");

        assertThat(result.status().value()).isEqualTo(403);
        assertThat(result.reply().errorCode()).isEqualTo("ORDER_NOT_AVAILABLE");
        assertThat(result.reply().response()).doesNotContain("Headphones");
    }

    @Test
    void createsHumanHandoffTicket() {
        when(tools.createTicket("CUST-1001", "请帮我转人工处理。"))
                .thenReturn(new TicketSnapshot("TKT-1001", "OPEN"));

        AgentResult result = agent.respond("CUST-1001", "session-1", "请帮我转人工处理。");

        assertThat(result.reply().intent()).isEqualTo("HUMAN_HANDOFF");
        assertThat(result.reply().ticketId()).isEqualTo("TKT-1001");
    }

    @Test
    void neverExecutesRefundAndUsesConversationOrderWhenAvailable() {
        when(memory.lastOrder("CUST-1001", "session-1")).thenReturn(Optional.of("ORD-10086"));
        when(tools.getOrder("CUST-1001", "ORD-10086"))
                .thenReturn(new OrderSnapshot("ORD-10086", "MacBook Case", "SHIPPED", "DHL", "DHL123456", "2026-09-02"));

        AgentResult result = agent.respond("CUST-1001", "session-1", "那可以退款吗？");

        assertThat(result.reply().intent()).isEqualTo("REFUND_REVIEW_REQUIRED");
        assertThat(result.reply().response()).contains("人工审核");
        verify(tools).getOrder("CUST-1001", "ORD-10086");
    }

    @Test
    void returnsOnlyCitedKnowledgeForPolicyQuestion() {
        String question = "可以取消订单吗？政策是什么？";
        when(knowledge.retrieve(question)).thenReturn(Optional.of(new KnowledgeAnswer(
                "订单取消需要人工处理。",
                List.of(new KnowledgeCitation("customer-service-policy-v1", "客户服务政策 v1",
                        "repository://knowledge/customer-service/policies-v1.md#refunds-and-order-changes",
                        LocalDate.of(2026, 8, 23))))));

        AgentResult result = agent.respond("CUST-1001", "session-1", question);

        assertThat(result.reply().intent()).isEqualTo("KNOWLEDGE_ANSWER");
        assertThat(result.reply().toolsCalled()).containsExactly("searchKnowledge");
        assertThat(result.reply().citations()).singleElement()
                .extracting(KnowledgeCitation::sourceKey).isEqualTo("customer-service-policy-v1");
        verify(knowledge).retrieve(question);
    }

    @Test
    void refusesUnknownKnowledgeWithoutFabricatingAnAnswer() {
        String question = "会员等级如何计算？";
        when(knowledge.retrieve(question)).thenReturn(Optional.empty());

        AgentResult result = agent.respond("CUST-1001", "session-1", question);

        assertThat(result.reply().intent()).isEqualTo("UNSUPPORTED");
        assertThat(result.reply().errorCode()).isEqualTo("KNOWLEDGE_SOURCE_NOT_FOUND");
        assertThat(result.reply().citations()).isEmpty();
        verify(knowledge).retrieve(question);
    }
}
