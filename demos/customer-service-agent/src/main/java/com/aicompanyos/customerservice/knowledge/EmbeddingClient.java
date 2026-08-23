package com.aicompanyos.customerservice.knowledge;

import java.util.List;

/** Provider-neutral port for generating dense embeddings outside the agent's authority boundary. */
public interface EmbeddingClient {
    List<EmbeddingVector> embed(List<String> inputs);
}
