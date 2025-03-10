package com.system.customer_service.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CustomerCode {

    private final RedisTemplate<String, Integer> redisTemplate;

    @Autowired
    public CustomerCode(@Qualifier("customerCodeRedisTemplate") RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Lưu code vào Redis với thời gian hết hạn
    public void saveCode(String mail, int code) {
        redisTemplate.opsForValue().set(mail, code, 10, TimeUnit.MINUTES);
    }

    // Lấy code từ Redis
    public Integer getCode(String mail) {
        return redisTemplate.opsForValue().get(mail);
    }

    // Xóa thông tin user (nếu cần)
    public void deleteCode(String mail) {
        redisTemplate.delete(mail);
    }
}
