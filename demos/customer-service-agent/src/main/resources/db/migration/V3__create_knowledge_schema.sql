CREATE TABLE knowledge_documents (
    id UUID PRIMARY KEY,
    source_key VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    source_uri VARCHAR(500) NOT NULL,
    published_at DATE NOT NULL,
    verified_at DATE NOT NULL,
    status VARCHAR(32) NOT NULL
);

CREATE INDEX knowledge_documents_status_idx ON knowledge_documents (status);

CREATE TABLE knowledge_chunks (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES knowledge_documents (id),
    chunk_index INTEGER NOT NULL,
    citation_anchor VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    CONSTRAINT knowledge_chunks_document_chunk_unique UNIQUE (document_id, chunk_index)
);

CREATE INDEX knowledge_chunks_document_id_idx ON knowledge_chunks (document_id);
