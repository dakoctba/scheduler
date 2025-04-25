package com.jacto.scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacto.scheduler.payload.response.SchedulingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisSchedulingServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisSchedulingService redisSchedulingService;

    private SchedulingResponse testScheduling;
    private Long schedulingId;

    @BeforeEach
    void setUp() {
        schedulingId = 1L;
        testScheduling = new SchedulingResponse();
        testScheduling.setId(schedulingId);
        testScheduling.setFarmName("Fazenda Teste");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void saveScheduling_ShouldSaveToRedis() {
        // Act
        redisSchedulingService.saveScheduling(testScheduling);

        // Assert
        verify(valueOperations).set(
            eq("scheduling:" + schedulingId),
            eq(testScheduling),
            eq(24L),
            eq(java.util.concurrent.TimeUnit.HOURS)
        );
    }

    @Test
    void getScheduling_WhenExists_ShouldReturnScheduling() {
        // Arrange
        when(valueOperations.get("scheduling:" + schedulingId)).thenReturn(testScheduling);
        when(objectMapper.convertValue(testScheduling, SchedulingResponse.class)).thenReturn(testScheduling);

        // Act
        SchedulingResponse result = redisSchedulingService.getScheduling(schedulingId);

        // Assert
        assertNotNull(result);
        assertEquals(schedulingId, result.getId());
        assertEquals("Fazenda Teste", result.getFarmName());
    }

    @Test
    void getScheduling_WhenNotExists_ShouldReturnNull() {
        // Arrange
        when(valueOperations.get("scheduling:" + schedulingId)).thenReturn(null);

        // Act
        SchedulingResponse result = redisSchedulingService.getScheduling(schedulingId);

        // Assert
        assertNull(result);
    }

    @Test
    void deleteScheduling_ShouldDeleteFromRedis() {
        // Act
        redisSchedulingService.deleteScheduling(schedulingId);

        // Assert
        verify(redisTemplate).delete("scheduling:" + schedulingId);
    }
}
