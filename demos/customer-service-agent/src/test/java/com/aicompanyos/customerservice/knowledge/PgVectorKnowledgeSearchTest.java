package com.aicompanyos.customerservice.knowledge;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PgVectorKnowledgeSearchTest {
    @Mock private JdbcTemplate jdbc;

    @Test
    void writesEmbeddingTimeAsSqlTimestamp() {
        UUID chunkId = UUID.randomUUID();
        EmbeddingVector vector = new EmbeddingVector(List.of(0.1, 0.2));

        new PgVectorKnowledgeSearch(jdbc).upsert(new PgVectorKnowledgeSearch.KnowledgeEmbedding(chunkId, "bge-m3", vector));

        verify(jdbc).update(anyString(), eq(chunkId), eq("bge-m3"), eq(2),
                eq(vector.pgVectorLiteral()), any(Timestamp.class));
    }
}
