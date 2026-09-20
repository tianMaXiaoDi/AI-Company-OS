package com.aicompanyos.customerservice.agent;

import java.util.Optional;

/**
 * Executes a model-requested refund-status lookup. Implementations must return
 * an empty result unless the authorized Java tool completed successfully.
 */
public interface RefundStatusToolCalling {
    Optional<String> reply(String customerId, String orderId, String customerMessage);
}
