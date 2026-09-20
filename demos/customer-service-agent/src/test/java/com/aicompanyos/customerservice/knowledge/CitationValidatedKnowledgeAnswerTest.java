package com.aicompanyos.customerservice.knowledge;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CitationValidatedKnowledgeAnswerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final KnowledgeAnswer retrieved = new KnowledgeAnswer("Orders can be cancelled only through human review.",
            List.of(new KnowledgeCitation("customer-service-policy-v1", "Customer Service Policy v1",
                    "repository://knowledge/customer-service/policies-v1.md#order-changes", LocalDate.of(2026, 8, 23))));

    @Test
    void acceptsOnlyCitationsThatCameFromRetrieval() {
        var actual = CitationValidatedKnowledgeAnswer.parse(mapper,
                "{\"answer\":\"订单取消需要人工审核。\",\"citations\":[\"S1\"]}", retrieved);

        assertThat(actual).isPresent();
        assertThat(actual.orElseThrow().response()).isEqualTo("订单取消需要人工审核。");
        assertThat(actual.orElseThrow().citations()).containsExactlyElementsOf(retrieved.citations());
    }

    @Test
    void rejectsAClaimWithAnInventedCitation() {
        var actual = CitationValidatedKnowledgeAnswer.parse(mapper,
                "{\"answer\":\"可以立即退款。\",\"citations\":[\"S2\"]}", retrieved);

        assertThat(actual).isEmpty();
    }

    @Test
    void rejectsAnUncitedOrMalformedModelResponse() {
        assertThat(CitationValidatedKnowledgeAnswer.parse(mapper,
                "{\"answer\":\"订单取消需要人工审核。\",\"citations\":[]}", retrieved)).isEmpty();
        assertThat(CitationValidatedKnowledgeAnswer.parse(mapper, "not json", retrieved)).isEmpty();
    }
}
