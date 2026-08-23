package com.aicompanyos.customerservice.knowledge;

import java.util.List;

public record EmbeddingVector(List<Double> values) {
    public EmbeddingVector {
        values = List.copyOf(values);
        if (values.isEmpty() || values.stream().anyMatch(value -> value == null || !Double.isFinite(value))) {
            throw new IllegalArgumentException("An embedding vector must contain finite values.");
        }
    }

    public int dimension() {
        return values.size();
    }

    public String pgVectorLiteral() {
        return values.stream().map(value -> value.toString()).collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }
}
