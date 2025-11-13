package com.quickbee.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeliveryQueueService {

    private final StringRedisTemplate redisTemplate;
    private static final String QUEUE_KEY = "delivery:queue";

    public DeliveryQueueService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void enqueueOrder(String orderId) {
        // RPUSH -> add to tail
        redisTemplate.opsForList().rightPush(QUEUE_KEY, orderId);
    }

    public void enqueueOrderFront(String orderId) {
        // LPUSH -> push to head (for immediate retry)
        redisTemplate.opsForList().leftPush(QUEUE_KEY, orderId);
    }

    public String popOldestOrder() {
        // LPOP -> pop from head (FIFO)
        return redisTemplate.opsForList().leftPop(QUEUE_KEY);
    }

    public Long queueLength() {
        return redisTemplate.opsForList().size(QUEUE_KEY);
    }
    public void removeOrderFromQueue(String orderId) {
        // remove all occurrences (count=0 means remove all)
        redisTemplate.opsForList().remove(QUEUE_KEY, 0, orderId);
    }

}
