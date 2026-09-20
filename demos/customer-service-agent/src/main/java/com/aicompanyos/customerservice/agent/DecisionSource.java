package com.aicompanyos.customerservice.agent;

/** The server records whether a request used the local model or a deterministic fallback. */
public enum DecisionSource {
    DETERMINISTIC,
    LLM_STRUCTURED,
    DETERMINISTIC_FALLBACK
}
