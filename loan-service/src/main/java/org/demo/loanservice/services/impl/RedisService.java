package org.demo.loanservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final String keyRedisKeyForm = "%s::%s";
    @Value("${key.redis.data.financialInfo}")
    private String keyFinancialInfo ;
    public void deleteCacheFinancialInfoDetailById(String financialInfoId) {
        redisTemplate.delete(String.format(keyRedisKeyForm,keyFinancialInfo,financialInfoId));
    }
    public void deleteCacheFinancialInfoDetailByFinancialInfoId(List<String> financialInfoIdList) {
        financialInfoIdList.forEach(financialInfoId -> {
            redisTemplate.delete(String.format(keyRedisKeyForm,keyFinancialInfo,financialInfoId));
        });
    }
}
