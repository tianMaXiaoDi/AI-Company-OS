package com.aicompanyos.customerservice.knowledge;

import java.time.Instant;
import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PgVectorKnowledgeSearch {
    private static final String NEAREST_PUBLISHED_CHUNK = """
            SELECT chunk.content, chunk.citation_anchor, document.source_key, document.title,
                   document.source_uri, document.verified_at,
                   embedding.embedding <=> CAST(? AS vector) AS distance
            FROM knowledge_chunk_embeddings embedding
            JOIN knowledge_chunks chunk ON chunk.id = embedding.chunk_id
            JOIN knowledge_documents document ON document.id = chunk.document_id
            WHERE document.status = 'PUBLISHED'
              AND embedding.model = ?
              AND embedding.dimensions = ?
            ORDER BY embedding.embedding <=> CAST(? AS vector)
            LIMIT 1
            """;

    private final JdbcTemplate jdbc;

    public PgVectorKnowledgeSearch(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<KnowledgeAnswer> findNearest(EmbeddingVector vector, String model, double maximumDistance) {
        String vectorLiteral = vector.pgVectorLiteral();
        List<SemanticMatch> matches = jdbc.query(NEAREST_PUBLISHED_CHUNK, (resultSet, rowNumber) -> new SemanticMatch(
                        resultSet.getString("content"), resultSet.getString("source_key"), resultSet.getString("title"),
                        resultSet.getString("source_uri") + "#" + resultSet.getString("citation_anchor"),
                        resultSet.getObject("verified_at", LocalDate.class), resultSet.getDouble("distance")),
                vectorLiteral, model, vector.dimension(), vectorLiteral);
        return matches.stream()
                .filter(match -> match.distance() <= maximumDistance)
                .findFirst()
                .map(match -> new KnowledgeAnswer(match.content(), List.of(new KnowledgeCitation(
                        match.sourceKey(), match.title(), match.sourceUri(), match.verifiedAt()))));
    }

    public void upsert(KnowledgeEmbedding embedding) {
        jdbc.update("""
                        INSERT INTO knowledge_chunk_embeddings (chunk_id, model, dimensions, embedding, embedded_at)
                        VALUES (?, ?, ?, CAST(? AS vector), ?)
                        ON CONFLICT (chunk_id, model) DO UPDATE SET
                            dimensions = EXCLUDED.dimensions,
                            embedding = EXCLUDED.embedding,
                            embedded_at = EXCLUDED.embedded_at
                        """,
                embedding.chunkId(), embedding.model(), embedding.vector().dimension(), embedding.vector().pgVectorLiteral(),
                Timestamp.from(Instant.now()));
    }

    public record KnowledgeEmbedding(java.util.UUID chunkId, String model, EmbeddingVector vector) {
    }

    private record SemanticMatch(String content, String sourceKey, String title, String sourceUri,
                                 LocalDate verifiedAt, double distance) {
    }
}
