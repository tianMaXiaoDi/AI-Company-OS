package com.aicompanyos.customerservice.knowledge;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticKnowledgeRetrieverTest {
    @Mock private CuratedKnowledgeRetriever lexicalFallback;
    @Mock private EmbeddingClient embeddings;
    @Mock private PgVectorKnowledgeSearch semanticSearch;

    private final SemanticKnowledgeProperties properties = new SemanticKnowledgeProperties(
            "http://localhost:8088", "", "BAAI/bge-m3", 1024, 0.45, false);

    @Test
    void returnsTheClosestCitedSemanticResult() {
        EmbeddingVector vector = new EmbeddingVector(List.of(0.1, 0.2));
        KnowledgeAnswer expected = new KnowledgeAnswer("订单取消需要人工处理。", List.of(new KnowledgeCitation(
                "customer-service-policy-v1", "客户服务政策 v1", "repository://policy#refunds", LocalDate.of(2026, 8, 23))));
        when(embeddings.embed(List.of("订单取消政策是什么？"))).thenReturn(List.of(vector));
        when(semanticSearch.findNearest(vector, "BAAI/bge-m3", 0.45)).thenReturn(Optional.of(expected));

        Optional<KnowledgeAnswer> actual = retriever().retrieve("订单取消政策是什么？");

        assertThat(actual).contains(expected);
        verify(semanticSearch).findNearest(vector, "BAAI/bge-m3", 0.45);
    }

    @Test
    void fallsBackWithoutSendingAnUnverifiedAnswerWhenTheLocalModelIsUnavailable() {
        KnowledgeAnswer fallback = new KnowledgeAnswer("退款需要人工审核。", List.of());
        when(embeddings.embed(any())).thenThrow(new EmbeddingUnavailableException("offline"));
        when(lexicalFallback.retrieve("退款政策是什么？")).thenReturn(Optional.of(fallback));

        Optional<KnowledgeAnswer> actual = retriever().retrieve("退款政策是什么？");

        assertThat(actual).contains(fallback);
        verify(lexicalFallback).retrieve("退款政策是什么？");
    }

    private SemanticKnowledgeRetriever retriever() {
        return new SemanticKnowledgeRetriever(lexicalFallback, embeddings, semanticSearch, properties);
    }
}
