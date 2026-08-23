# ADR-004: Citation-First Knowledge Retrieval Before Embeddings

Date: 2026-08-23  
Status: Accepted

## Context

The customer service agent may not state policy facts without a verifiable source. An embedding model, its vector dimension, provider credentials, retention terms, evaluation set, and operating cost have not been selected. Introducing a model or a vector schema first would add an external dependency before the knowledge-governance boundary is proven.

## Decision

1. Store curated knowledge as versioned source documents and immutable, answer-ready chunks in PostgreSQL.
2. Retrieve only documents whose status is `PUBLISHED`. The baseline ranks matching terms and returns the selected chunk verbatim with its source key, URI, and verification date.
3. The retrieval port is read-only. It has no access to orders, customer data, payment, or ticket mutation.
4. If no verified source matches, return no knowledge answer. The agent must state that a verified source was not found and may route the user to human support.
5. Keep `pgvector` enabled but defer `vector(n)` columns and embedding generation until an embedding model and evaluation criteria are explicitly chosen.

## Consequences

- The first policy-answer flow is auditable and cannot fabricate a policy statement.
- The current matching is deliberately narrow; it is a correctness baseline, not a semantic-search claim.
- A future embedding adapter can implement the same `KnowledgeRetriever` port without expanding the agent's authority.
