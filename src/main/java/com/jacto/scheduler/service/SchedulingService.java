package com.jacto.scheduler.service;

import com.jacto.scheduler.enumerations.SchedulingStatus;
import com.jacto.scheduler.enumerations.ServicePriority;
import com.jacto.scheduler.exception.ResourceNotFoundException;
import com.jacto.scheduler.model.*;
import com.jacto.scheduler.payload.request.*;
import com.jacto.scheduler.payload.response.GeoLocationDetails;
import com.jacto.scheduler.payload.response.SchedulingResponse;
import com.jacto.scheduler.payload.response.TechnicianPerformanceResponse;
import com.jacto.scheduler.repository.EquipmentRepository;
import com.jacto.scheduler.repository.SchedulingRepository;
import com.jacto.scheduler.repository.SparePartRepository;
import com.jacto.scheduler.repository.UserRepository;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchedulingService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingService.class);

    private final SchedulingRepository schedulingRepository;
    private final EquipmentRepository equipmentRepository;
    private final SparePartRepository sparePartRepository;
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;
    private final NotificationService notificationService;
    private final RedisSchedulingService redisSchedulingService;
    private final MetricsService metricsService;

    public SchedulingService(
            SchedulingRepository schedulingRepository,
            EquipmentRepository equipmentRepository,
            SparePartRepository sparePartRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            GeocodingService geocodingService,
            RedisSchedulingService redisSchedulingService,
            MetricsService metricsService) {
        this.schedulingRepository = schedulingRepository;
        this.equipmentRepository = equipmentRepository;
        this.sparePartRepository = sparePartRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.geocodingService = geocodingService;
        this.redisSchedulingService = redisSchedulingService;
        this.metricsService = metricsService;
    }

    public List<SchedulingResponse> getAllSchedulingsForCurrentUser() {
        User currentUser = getCurrentUser();

        List<Scheduling> schedulings = schedulingRepository.findByTechnicianOrderByScheduledAtDesc(currentUser);

        return schedulings.stream()
                .map(scheduling -> {
                    // Sempre converter e enriquecer com dados do banco
                    SchedulingResponse response = new SchedulingResponse(scheduling);
                    enrichWithGeocodingData(response);

                    // Atualizar o cache com os dados mais recentes
                    redisSchedulingService.saveScheduling(response);

                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<SchedulingResponse> getUpcomingSchedulingsForCurrentUser() {
        User currentUser = getCurrentUser();

        List<Scheduling> schedulings = schedulingRepository.findUpcomingSchedulings(
                currentUser, LocalDateTime.now());

        return schedulings.stream()
                .map(scheduling -> {
                    // Sempre converter e enriquecer com dados do banco
                    SchedulingResponse response = new SchedulingResponse(scheduling);
                    enrichWithGeocodingData(response);

                    // Atualizar o cache com os dados mais recentes
                    redisSchedulingService.saveScheduling(response);

                    return response;
                })
                .collect(Collectors.toList());
    }

    public SchedulingResponse getSchedulingById(Long id) {
        User currentUser = getCurrentUser();

        // Tentar obter do cache primeiro
        SchedulingResponse cachedScheduling = redisSchedulingService.getScheduling(id);
        if (cachedScheduling != null) {
            return cachedScheduling;
        }

        // Se não estiver no cache, buscar no banco de dados
        Scheduling scheduling = schedulingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com id: " + id));

        // Verifica se o agendamento pertence ao usuário atual
        if (!scheduling.getTechnician().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Agendamento não encontrado com id: " + id);
        }

        // Converter para resposta e enriquecer com dados de geolocalização
        SchedulingResponse response = new SchedulingResponse(scheduling);
        enrichWithGeocodingData(response);

        // Salvar no cache para futuras consultas
        redisSchedulingService.saveScheduling(response);

        return response;
    }

    public SchedulingResponse getSchedulingByIdForKafka(Long id) {
        // Tentar obter do cache primeiro
        SchedulingResponse cachedScheduling = redisSchedulingService.getScheduling(id);
        if (cachedScheduling != null) {
            return cachedScheduling;
        }

        // Se não estiver no cache, buscar no banco de dados
        Scheduling scheduling = schedulingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com id: " + id));

        // Converter para resposta e enriquecer com dados de geolocalização
        SchedulingResponse response = new SchedulingResponse(scheduling);
        enrichWithGeocodingData(response);

        // Salvar no cache para futuras consultas
        redisSchedulingService.saveScheduling(response);

        return response;
    }

    @Transactional
    public SchedulingResponse createScheduling(SchedulingRequest request) {
        Timer.Sample sample = metricsService.startSchedulingDuration();
        try {
            User currentUser = getCurrentUser();

            // Validar se a data do agendamento não é anterior à data atual
            if (request.getScheduledAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("A data de agendamento não pode ser anterior à data atual");
            }

            // Criar um novo agendamento
            Scheduling scheduling = new Scheduling();
            scheduling.setTechnician(currentUser);
            scheduling.setFarmName(request.getFarmName());
            scheduling.setClientName(request.getClientName());
            scheduling.setClientEmail(request.getClientEmail());
            scheduling.setAddress(request.getAddress());
            scheduling.setLatitude(request.getLatitude());
            scheduling.setLongitude(request.getLongitude());
            scheduling.setScheduledAt(request.getScheduledAt());
            scheduling.setServiceDescription(request.getServiceDescription());
            scheduling.setStatus(SchedulingStatus.PENDING);

            // Definir prioridade
            if (request.getPriority() != null) {
                try {
                    scheduling.setPriority(ServicePriority.valueOf(request.getPriority().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Usar valor padrão se a prioridade for inválida
                    scheduling.setPriority(ServicePriority.MEDIUM);
                }
            }

            // Salvar o agendamento primeiro para obter o ID
            scheduling = schedulingRepository.save(scheduling);

            // Registrar métricas
            metricsService.incrementSchedulingTotal();
            metricsService.incrementSchedulingStatus(scheduling.getStatus());

            // Processar equipamentos, se houver
            if (request.getEquipments() != null && !request.getEquipments().isEmpty()) {
                for (EquipmentRequest equipmentRequest : request.getEquipments()) {
                    Equipment equipment = new Equipment();
                    equipment.setScheduling(scheduling);
                    equipment.setName(equipmentRequest.getName());
                    equipment.setSerialNumber(equipmentRequest.getSerialNumber());
                    equipment.setDescription(equipmentRequest.getDescription());
                    equipmentRepository.save(equipment);
                }
            }

            // Processar peças de reposição, se houver
            if (request.getSpareParts() != null && !request.getSpareParts().isEmpty()) {
                for (SparePartRequest sparePartRequest : request.getSpareParts()) {
                    SparePart sparePart = new SparePart();
                    sparePart.setScheduling(scheduling);
                    sparePart.setName(sparePartRequest.getName());
                    sparePart.setPartNumber(sparePartRequest.getPartNumber());
                    sparePart.setQuantity(sparePartRequest.getQuantity());
                    sparePartRepository.save(sparePart);
                }
            }

            // Buscar o agendamento completo com relacionamentos
            scheduling = schedulingRepository.findById(scheduling.getId()).orElseThrow();

            // Retornar resposta com dados de geolocalização
            SchedulingResponse response = new SchedulingResponse(scheduling);
            enrichWithGeocodingData(response);

            // Salvar no cache
            redisSchedulingService.saveScheduling(response);

            // Enviar notificação assíncrona
            notificationService.sendSchedulingCreatedNotification(scheduling.getId());

            return response;
        } finally {
            metricsService.stopSchedulingDuration(sample);
        }
    }

    @Transactional
    public SchedulingResponse updateScheduling(Long id, SchedulingUpdateRequest request) {
        Timer.Sample sample = metricsService.startSchedulingDuration();
        try {
            User currentUser = getCurrentUser();

            Scheduling scheduling = schedulingRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com id: " + id));

            // Verifica se o agendamento pertence ao usuário atual
            if (!scheduling.getTechnician().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Agendamento não encontrado com id: " + id);
            }

            // Validar se a data do agendamento não é anterior à data atual
            if (request.getScheduledAt() != null && request.getScheduledAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("A data de agendamento não pode ser anterior à data atual");
            }

            // Atualizar os campos do agendamento
            if (request.getFarmName() != null) {
                scheduling.setFarmName(request.getFarmName());
            }

            if (request.getClientName() != null) {
                scheduling.setClientName(request.getClientName());
            }

            if (request.getClientEmail() != null) {
                scheduling.setClientEmail(request.getClientEmail());
            }

            if (request.getAddress() != null) {
                scheduling.setAddress(request.getAddress());
            }

            if (request.getScheduledAt() != null) {
                scheduling.setScheduledAt(request.getScheduledAt());
            }

            if (request.getServiceDescription() != null) {
                scheduling.setServiceDescription(request.getServiceDescription());
            }

            if (request.getStatus() != null) {
                scheduling.setStatus(SchedulingStatus.valueOf(request.getStatus().toUpperCase()));
                metricsService.incrementSchedulingStatus(scheduling.getStatus());
            }

            // Salvar as alterações
            scheduling = schedulingRepository.save(scheduling);

            // Retornar resposta com dados de geolocalização
            SchedulingResponse response = new SchedulingResponse(scheduling);
            enrichWithGeocodingData(response);

            // Atualizar o cache
            redisSchedulingService.saveScheduling(response);

            return response;
        } finally {
            metricsService.stopSchedulingDuration(sample);
        }
    }

    @Transactional
    public void deleteScheduling(Long id) {
        User currentUser = getCurrentUser();

        Scheduling scheduling = schedulingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com id: " + id));

        // Verifica se o agendamento pertence ao usuário atual
        if (!scheduling.getTechnician().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Agendamento não encontrado com id: " + id);
        }

        // Remover o agendamento (e todos os relacionamentos em cascata)
        schedulingRepository.delete(scheduling);

        // Remover do cache
        redisSchedulingService.deleteScheduling(id);
    }

    @Transactional
    public SchedulingResponse addClientFeedback(Long id, ClientFeedbackRequest request) {
        Scheduling scheduling = schedulingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com id: " + id));

        // Verificar se o agendamento está concluído
        if (scheduling.getStatus() != SchedulingStatus.COMPLETED) {
            throw new IllegalStateException("Feedback só pode ser adicionado a agendamentos concluídos");
        }

        // Adicionar feedback e avaliação
        scheduling.setClientRating(request.getRating());
        scheduling.setClientFeedback(request.getFeedback());

        // Salvar as alterações
        scheduling = schedulingRepository.save(scheduling);

        // Retornar resposta atualizada
        SchedulingResponse response = new SchedulingResponse(scheduling);
        enrichWithGeocodingData(response);

        return response;
    }

    public TechnicianPerformanceResponse getTechnicianPerformance(Long technicianId) {
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado com id: " + technicianId));

        TechnicianPerformanceResponse performance = new TechnicianPerformanceResponse();
        performance.setTechnicianId(technician.getId());
        performance.setTechnicianName(technician.getFullName());

        // Calcular métricas de desempenho
        performance.setAverageRating(schedulingRepository.findAverageRatingForTechnician(technician));
        performance.setCompletedVisits(schedulingRepository.countCompletedSchedulings(technician));
        performance.setAverageVisitDuration(schedulingRepository.findAverageCompletionTimeForTechnician(technician));

        return performance;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
    }

    private void enrichWithGeocodingData(SchedulingResponse response) {
        try {
            GeoLocationDetails locationDetails = geocodingService.getLocationDetails(
                    response.getLatitude(), response.getLongitude());
            response.setLocationDetails(locationDetails);
        } catch (Exception e) {
            logger.error("Não foi possível enriquecer o endereço do agendamento com dados de geolocalização");
        }
    }
}
