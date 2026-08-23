CREATE TABLE knowledge_chunk_embeddings (
    chunk_id UUID NOT NULL REFERENCES knowledge_chunks (id),
    model VARCHAR(120) NOT NULL,
    dimensions INTEGER NOT NULL CHECK (dimensions = 1024),
    embedding vector(1024) NOT NULL,
    embedded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (chunk_id, model)
);

CREATE INDEX knowledge_chunk_embeddings_hnsw_idx
    ON knowledge_chunk_embeddings USING hnsw (embedding vector_cosine_ops);
