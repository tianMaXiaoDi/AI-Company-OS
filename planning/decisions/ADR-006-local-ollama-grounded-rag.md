# ADR-006: Generate Grounded Knowledge Answers with Local Ollama

Date: 2026-08-24  
Status: Accepted

## Context

ADR-004 established that policy answers require a published source and server-owned citations. ADR-005 added an opt-in local semantic retriever, but its result is still raw knowledge-chunk text. The intended customer experience is a RAG flow: retrieve a relevant chunk, allow an LLM to read it, and return a concise answer with citations. This must not weaken the existing authorization and tool boundaries.

## Decision

1. Use Ollama on a loopback-only endpoint as the initial local generation adapter. The opt-in `llm` Spring profile defaults to `qwen3:4b` at `http://localhost:11434/api/chat` and uses a structured JSON response.
2. The LLM is invoked only after `KnowledgeRetriever` has found a published, citation-backed result. It receives the question and that result's knowledge text; it does not receive repositories, database credentials, tool APIs, order data, customer identity, or unpublished knowledge.
3. The service assigns source IDs (`S1`, `S2`, ...) to retrieved citations. The model must choose at least one of those IDs. The service rejects malformed output, duplicate IDs, missing IDs, or IDs outside that set.
4. On any Ollama transport, response, or citation-validation failure, return the original retrieved knowledge text and its server-owned citations. An unavailable model cannot turn a verified answer into an outage or cause an uncited answer.
5. Generation remains a read-only presentation step. It is not a tool, cannot choose business tools, and cannot execute refunds, order changes, payment changes, or other mutations.

## Consequences

- Running with `dev,semantic,llm` provides the full local RAG path while `dev` continues to provide the deterministic citation-first baseline.
- The server remains the authority for retrieval, source eligibility, citations, identity, authorization, and business facts.
- The local Ollama dependency and model quality need offline evaluation before production use. A remote model provider requires a separate data-processing and security ADR.
