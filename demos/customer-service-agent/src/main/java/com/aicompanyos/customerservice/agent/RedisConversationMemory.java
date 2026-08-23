package com.aicompanyos.customerservice.agent;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Stores only the minimum conversation reference needed for follow-up order questions. */
@Service
public class RedisConversationMemory {
    private static final Duration TTL = Duration.ofHours(8);
    private final StringRedisTemplate redis;

    public RedisConversationMemory(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void rememberOrder(String customerId, String sessionId, String orderId) {
        redis.opsForValue().set(key(customerId, sessionId), orderId, TTL);
    }

    public Optional<String> lastOrder(String customerId, String sessionId) {
        return Optional.ofNullable(redis.opsForValue().get(key(customerId, sessionId)));
    }

    private String key(String customerId, String sessionId) {
        return "customer-service:session:" + customerId + ":" + sessionId + ":last-order";
    }
}
