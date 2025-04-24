package com.jacto.scheduler.service;

import com.jacto.scheduler.config.KafkaConfig;
import com.jacto.scheduler.model.Scheduling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendSchedulingCreatedNotification(Long schedulingId) {
        kafkaTemplate.send(KafkaConfig.SCHEDULING_CREATED_TOPIC, schedulingId);
    }

    public void sendSchedulingUpdatedNotification(Long schedulingId) {
        kafkaTemplate.send(KafkaConfig.SCHEDULING_UPDATED_TOPIC, schedulingId);
    }

    public void sendSchedulingDeletedNotification(Long schedulingId) {
        kafkaTemplate.send(KafkaConfig.SCHEDULING_DELETED_TOPIC, schedulingId);
    }

    public void sendSchedulingReminderNotification(Long schedulingId) {
        kafkaTemplate.send(KafkaConfig.SCHEDULING_REMINDER_TOPIC, schedulingId);
    }
}
