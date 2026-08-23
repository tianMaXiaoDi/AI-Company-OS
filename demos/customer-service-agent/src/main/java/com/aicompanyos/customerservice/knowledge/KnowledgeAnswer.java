package com.aicompanyos.customerservice.knowledge;

import java.util.List;

public record KnowledgeAnswer(String response, List<KnowledgeCitation> citations) {
    public KnowledgeAnswer {
        citations = List.copyOf(citations);
    }
}
