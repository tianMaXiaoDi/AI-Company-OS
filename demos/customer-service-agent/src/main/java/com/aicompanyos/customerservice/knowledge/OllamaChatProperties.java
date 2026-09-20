package com.aicompanyos.customerservice.knowledge;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "customer-service.knowledge.llm")
public record OllamaChatProperties(
        String baseUrl,
        String model,
        int maximumTokens,
        double temperature) {

    public OllamaChatProperties {
        baseUrl = baseUrl == null || baseUrl.isBlank() ? "http://localhost:11434" : baseUrl.strip();
        model = model == null || model.isBlank() ? "qwen3:4b" : model.strip();
        maximumTokens = maximumTokens == 0 ? 400 : maximumTokens;
        temperature = temperature == 0 ? 0 : temperature;
        if (maximumTokens < 1 || maximumTokens > 2_000) {
            throw new IllegalArgumentException("maximumTokens must be between 1 and 2000.");
        }
        if (temperature < 0 || temperature > 2) {
            throw new IllegalArgumentException("temperature must be between 0 and 2.");
        }
    }
}
