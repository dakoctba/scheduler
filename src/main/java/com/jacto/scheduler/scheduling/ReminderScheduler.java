package com.jacto.scheduler.scheduling;

import com.jacto.scheduler.model.Scheduling;
import com.jacto.scheduler.enumerations.SchedulingStatus;
import com.jacto.scheduler.repository.SchedulingRepository;
import com.jacto.scheduler.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);

    private final SchedulingRepository schedulingRepository;
    private final NotificationService notificationService;

    public ReminderScheduler(SchedulingRepository schedulingRepository, NotificationService notificationService) {
        this.schedulingRepository = schedulingRepository;
        this.notificationService = notificationService;
    }

    // Descomentar para testar agendamentos próximos
//    @EventListener(ApplicationReadyEvent.class)
//    public void triggerOnStartup() {
//        sendReminders();
//    }

    // Executa a cada hora para verificar agendamentos próximos
    @Scheduled(cron = "0 0 * * * *")
    public void sendReminders() {
        logger.info("Verificando agendamentos para envio de lembretes");

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1).withHour(23).withMinute(59).withSecond(59);

        // Buscar agendamentos para o próximo dia que ainda não foram cancelados
        List<Scheduling> upcomingSchedulings = schedulingRepository.findSchedulingsForDateRange(
                start, end);

        for (Scheduling scheduling : upcomingSchedulings) {
            // Pular agendamentos cancelados
            if (scheduling.getStatus() == SchedulingStatus.CANCELLED) {
                continue;
            }

            // Enviar lembrete
            notificationService.sendSchedulingReminderNotification(scheduling.getId());
            logger.info("Lembrete enviado para agendamento ID={}", scheduling.getId());
        }
    }
}
