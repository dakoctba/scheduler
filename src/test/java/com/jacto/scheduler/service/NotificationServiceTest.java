package com.jacto.scheduler.service;

import com.jacto.scheduler.config.KafkaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private Long schedulingId;

    @BeforeEach
    void setUp() {
        schedulingId = 1L;
    }

    @Test
    void sendSchedulingCreatedNotification_ShouldSendToKafka() {
        // Act
        notificationService.sendSchedulingCreatedNotification(schedulingId);

        // Assert
        verify(kafkaTemplate).send(eq(KafkaConfig.SCHEDULING_CREATED_TOPIC), eq(schedulingId));
    }

    @Test
    void sendSchedulingUpdatedNotification_ShouldSendToKafka() {
        // Act
        notificationService.sendSchedulingUpdatedNotification(schedulingId);

        // Assert
        verify(kafkaTemplate).send(eq(KafkaConfig.SCHEDULING_UPDATED_TOPIC), eq(schedulingId));
    }

    @Test
    void sendSchedulingDeletedNotification_ShouldSendToKafka() {
        // Act
        notificationService.sendSchedulingDeletedNotification(schedulingId);

        // Assert
        verify(kafkaTemplate).send(eq(KafkaConfig.SCHEDULING_DELETED_TOPIC), eq(schedulingId));
    }

    @Test
    void sendSchedulingReminderNotification_ShouldSendToKafka() {
        // Act
        notificationService.sendSchedulingReminderNotification(schedulingId);

        // Assert
        verify(kafkaTemplate).send(eq(KafkaConfig.SCHEDULING_REMINDER_TOPIC), eq(schedulingId));
    }
}
