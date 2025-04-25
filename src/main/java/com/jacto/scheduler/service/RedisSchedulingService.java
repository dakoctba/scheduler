package com.jacto.scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacto.scheduler.payload.response.SchedulingResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisSchedulingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String SCHEDULING_KEY_PREFIX = "scheduling:";
    private static final long CACHE_EXPIRATION = 24; // 24 horas

    public RedisSchedulingService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void saveScheduling(SchedulingResponse scheduling) {
        String key = SCHEDULING_KEY_PREFIX + scheduling.getId();
        redisTemplate.opsForValue().set(key, scheduling, CACHE_EXPIRATION, TimeUnit.HOURS);
    }

    public SchedulingResponse getScheduling(Long id) {
        String key = SCHEDULING_KEY_PREFIX + id;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return objectMapper.convertValue(value, SchedulingResponse.class);
    }

    public void deleteScheduling(Long id) {
        String key = SCHEDULING_KEY_PREFIX + id;
        redisTemplate.delete(key);
    }
}
