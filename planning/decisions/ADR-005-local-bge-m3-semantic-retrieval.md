# ADR-005: Use an Opt-In Local BGE-M3 Semantic Retrieval Profile

Date: 2026-08-23  
Status: Accepted

## Context

The citation-first retrieval baseline is correct but narrow: it relies on matching terms and cannot resolve paraphrases reliably. Customer-service traffic is primarily Chinese, while policy content may become bilingual. A semantic index is needed without adopting Spring AI or sending customer text to an unapproved third-party service.

## Decision

1. Use the dense output of `BAAI/bge-m3` as the initial semantic model contract: 1024 dimensions, hosted through a local OpenAI-compatible `/v1/embeddings` endpoint.
2. Add `knowledge_chunk_embeddings` with `vector(1024)` and an HNSW cosine-distance index. Vectors are tied to both chunk and model name, so an explicitly selected future model can be re-indexed separately.
3. The `semantic` Spring profile is opt-in. It is disabled in local `dev` startup and rejects any endpoint other than loopback addresses.
4. Re-indexing is explicit through `customer-service.knowledge.semantic.index-on-startup=true`; ordinary application startup never invokes an embedding model.
5. Semantic results retain the same `KnowledgeRetriever` contract and citation metadata. An unavailable local model or a low-confidence vector match falls back to the existing citation-first lexical retrieval.

## Consequences

- The application does not need Spring AI or an external provider SDK; it uses a narrow HTTP adapter behind `EmbeddingClient`.
- No remote model receives customer messages, order data, or policy text under this ADR.
- `BAAI/bge-m3` is multilingual and exposes a 1024-dimensional dense embedding, which matches the fixed pgvector column. The exact model revision and retrieval threshold must be evaluated before production use.
