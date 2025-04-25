package com.jacto.scheduler.messaging;

import com.jacto.scheduler.config.KafkaConfig;
import com.jacto.scheduler.payload.response.SchedulingResponse;
import com.jacto.scheduler.service.MailService;
import com.jacto.scheduler.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class SchedulingNotificationConsumer {

    private final SchedulingService schedulingService;
    private final MailService mailService;
    private static final Logger logger = LoggerFactory.getLogger(SchedulingNotificationConsumer.class);

    public SchedulingNotificationConsumer(SchedulingService schedulingService, MailService mailService) {
        this.schedulingService = schedulingService;
        this.mailService = mailService;
    }

    @KafkaListener(topics = KafkaConfig.SCHEDULING_CREATED_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleSchedulingCreated(Long schedulingId) {
        SchedulingResponse scheduling = schedulingService.getSchedulingByIdForKafka(schedulingId);

        logger.info("Recebida notificação de agendamento criado: ID={}, Cliente={}, Data={}",
                scheduling.getId(), scheduling.getClientName(), scheduling.getScheduledAt());

        // Envia e-mail para cliente
        String text = this.formatConfirmationEmail(scheduling);
        mailService.sendEmail(scheduling.getClientEmail(), "Confirmação de Agendamento - Visita Técnica Jacto", text);

        // Envia e-mail para técnico
        String technicianText = this.formatTechnicianNotificationEmail(scheduling);
        mailService.sendEmail(scheduling.getTechnicianEmail(), "Novo Agendamento Atribuído - Visita Técnica Jacto", technicianText);
    }

    @KafkaListener(topics = KafkaConfig.SCHEDULING_UPDATED_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleSchedulingUpdated(Long schedulingId) {
        SchedulingResponse scheduling = schedulingService.getSchedulingByIdForKafka(schedulingId);

        logger.info("Recebida notificação de agendamento atualizado: ID={}, Status={}",
                scheduling.getId(), scheduling.getStatus());
    }

    @KafkaListener(topics = KafkaConfig.SCHEDULING_REMINDER_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleSchedulingReminder(Long schedulingId) {
        SchedulingResponse scheduling = schedulingService.getSchedulingByIdForKafka(schedulingId);

        logger.info("Enviando lembrete para agendamentos de amanhã: ID={}, Data={}",
                scheduling.getId(), scheduling.getScheduledAt());
    }

    private String formatConfirmationEmail(SchedulingResponse scheduling) {
        String emailTemplate = """
        Assunto: Confirmação de Agendamento - Visita Técnica Jacto

        Prezado(a) %s,

        Seu agendamento de visita técnica foi confirmado com sucesso!

        Detalhes do Agendamento:
        - Número do Agendamento: #%d
        - Data: %s
        - Local: %s
        - Endereço: %s
        - Técnico Responsável: %s
        - Descrição do Serviço: %s
        - Prioridade: %s

        %s

        Para mais detalhes ou se precisar reagendar, entre em contato conosco.

        Atenciosamente,
        Equipe de Atendimento Técnico
        Jacto
        """;

        String equipmentSection = "";
        if (!scheduling.getEquipments().isEmpty()) {
            equipmentSection = "Equipamentos para manutenção:\n" +
                    scheduling.getEquipments().stream()
                            .map(e -> "- " + e.getName() + " (SerialNumber: " + e.getSerialNumber() + ")")
                            .collect(Collectors.joining("\n")) + "\n";
        }

        return String.format(emailTemplate,
                scheduling.getClientName(),
                scheduling.getId(),
                scheduling.getScheduledAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                scheduling.getFarmName(),
                scheduling.getAddress(),
                scheduling.getTechnicianFullName(),
                scheduling.getServiceDescription(),
                scheduling.getPriority(),
                equipmentSection);
    }

    private String formatTechnicianNotificationEmail(SchedulingResponse scheduling) {
        String emailTemplate = """
            Assunto: Novo Agendamento Atribuído - Visita Técnica Jacto

            Olá %s,

            Um novo agendamento foi atribuído a você.

            Detalhes do Agendamento:
            - Número do Agendamento: #%d
            - Cliente: %s
            - Data: %s
            - Local: %s
            - Endereço: %s
            - Descrição do Serviço: %s
            - Prioridade: %s

            %s

            Por favor, prepare-se para essa visita e entre em contato com a equipe de suporte caso haja qualquer dúvida.

            Atenciosamente,
            Equipe de Atendimento Técnico
            Jacto
            """;

        String equipmentSection = "";
        if (!scheduling.getEquipments().isEmpty()) {
            equipmentSection = "Equipamentos para manutenção:\n" +
                    scheduling.getEquipments().stream()
                            .map(e -> "- " + e.getName() + " (SerialNumber: " + e.getSerialNumber() + ")")
                            .collect(Collectors.joining("\n")) + "\n";
        }

        return String.format(emailTemplate,
                scheduling.getTechnicianFullName(),
                scheduling.getId(),
                scheduling.getClientName(),
                scheduling.getScheduledAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                scheduling.getFarmName(),
                scheduling.getAddress(),
                scheduling.getServiceDescription(),
                scheduling.getPriority(),
                equipmentSection);
    }
}
