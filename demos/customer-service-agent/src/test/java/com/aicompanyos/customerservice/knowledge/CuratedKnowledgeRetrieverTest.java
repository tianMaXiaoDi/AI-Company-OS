package com.aicompanyos.customerservice.knowledge;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuratedKnowledgeRetrieverTest {
    @Mock private KnowledgeChunkRepository chunks;

    @Test
    void returnsTheBestMatchingPublishedChunkWithCitation() {
        KnowledgeDocument document = new KnowledgeDocument(
                UUID.randomUUID(), "customer-service-policy-v1", "客户服务政策 v1",
                "repository://knowledge/customer-service/policies-v1.md", LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 23), KnowledgeDocumentStatus.PUBLISHED);
        KnowledgeChunk refund = new KnowledgeChunk(UUID.randomUUID(), document, 1, "refunds-and-order-changes",
                "退款申请必须进入人工审核，客服 Agent 不会自动执行退款。订单取消同样需要人工处理。");
        KnowledgeChunk shipping = new KnowledgeChunk(UUID.randomUUID(), document, 2, "order-and-shipping-queries",
                "客户可以查询本人订单的物流状态。");
        when(chunks.findByDocumentStatus(KnowledgeDocumentStatus.PUBLISHED)).thenReturn(List.of(shipping, refund));

        Optional<KnowledgeAnswer> answer = new CuratedKnowledgeRetriever(chunks).retrieve("退款政策是什么？");

        assertThat(answer).isPresent();
        assertThat(answer.orElseThrow().response()).contains("人工审核");
        assertThat(answer.orElseThrow().citations()).singleElement().satisfies(citation -> {
            assertThat(citation.sourceUri()).endsWith("#refunds-and-order-changes");
            assertThat(citation.verifiedAt()).isEqualTo(LocalDate.of(2026, 8, 23));
        });
    }

    @Test
    void returnsNothingWhenNoPublishedSourceMatches() {
        when(chunks.findByDocumentStatus(KnowledgeDocumentStatus.PUBLISHED)).thenReturn(List.of());

        Optional<KnowledgeAnswer> answer = new CuratedKnowledgeRetriever(chunks).retrieve("你们的会员等级如何计算？");

        assertThat(answer).isEmpty();
    }
}
