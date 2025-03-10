package com.system.customer_service.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Service
public class CustomerLoginAttempt {
    private final RedisTemplate<String, Integer> redisTemplate;

    public CustomerLoginAttempt(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Lưu code vào Redis với thời gian hết hạn
    public void saveAttempt(String customerPhone, int attempts) {
        redisTemplate.opsForValue().set(customerPhone, attempts, 1, TimeUnit.DAYS);
    }

    // Lấy code từ Redis
    public Integer getAttempt(String customerPhone) {
        return redisTemplate.opsForValue().get(customerPhone);
    }

    // Reset số lần nhập sai về 0
    public void resetAttempt(String customerPhone) {
        redisTemplate.delete(customerPhone); // Xóa key khỏi Redis
    }
}
