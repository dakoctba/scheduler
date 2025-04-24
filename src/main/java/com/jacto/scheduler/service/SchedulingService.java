package com.jacto.scheduler.service;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchedulingService {

    private final SchedulingRepository schedulingRepository;
    private final EquipmentRepository equipmentRepository;
    private final SparePartRepository sparePartRepository;
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;
    private final NotificationService notificationService;

    public SchedulingService(
            SchedulingRepository schedulingRepository,
            EquipmentRepository equipmentRepository,
            SparePartRepository sparePartRepository,
            UserRepository userRepository,
            GeocodingService geocodingService,
            NotificationService notificationService) {
        this.schedulingRepository = schedulingRepository;
        this.equipmentRepository = equipmentRepository;
        this.sparePartRepository = sparePartRepository;
        this.userRepository = userRepository;
        this.geocodingService = geocodingService;
        this.notificationService = notificationService;
    }

    public List<SchedulingResponse> getAllSchedulingsForCurrentUser() {
        User currentUser = getCurrentUser();

        List<Scheduling> schedulings = schedulingRepository.findByTechnicianOrderByScheduledAtDesc(currentUser);

        return schedulings.stream()
                .map(scheduling -> {
                    SchedulingResponse response = new SchedulingResponse(scheduling);
                    enrichWithGeocodingData(response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<SchedulingResponse> getUpcomingSchedulings() {
        User currentUser = getCurrentUser();

        List<Scheduling> schedulings = schedulingRepository.findUpcomingSchedulings(
                currentUser, LocalDateTime.now());

        return schedulings.stream()
                .map(scheduling -> {
                    SchedulingResponse response = new SchedulingResponse(scheduling);
                    enrichWithGeocodingData(response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    public SchedulingResponse getSchedulingById(Long id) {
        User currentUser = getCurrentUser();

        Scheduling scheduling = schedulingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com id: " + id));

        // Verifica se o agendamento pertence ao usuário atual
        if (!scheduling.getTechnician().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Agendamento não encontrado com id: " + id);
        }

        SchedulingResponse response = new SchedulingResponse(scheduling);
        enrichWithGeocodingData(response);

        return response;
    }

    public SchedulingResponse getSchedulingByIdForKafka(Long id) {
        Scheduling scheduling = schedulingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com id: " + id));

        SchedulingResponse response = new SchedulingResponse(scheduling);
        enrichWithGeocodingData(response);

        return response;
    }

    @Transactional
    public SchedulingResponse createScheduling(SchedulingRequest request) {
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

        // Enviar notificação assíncrona
        notificationService.sendSchedulingCreatedNotification(scheduling.getId());

        // Retornar resposta com dados de geolocalização
        SchedulingResponse response = new SchedulingResponse(scheduling);
        enrichWithGeocodingData(response);

        return response;
    }

    @Transactional
    public SchedulingResponse updateScheduling(Long id, SchedulingUpdateRequest request) {
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

        if (request.getLatitude() != null && request.getLongitude() != null) {
            scheduling.setLatitude(request.getLatitude());
            scheduling.setLongitude(request.getLongitude());
        }

        if (request.getScheduledAt() != null) {
            scheduling.setScheduledAt(request.getScheduledAt());
        }

        if (request.getServiceDescription() != null) {
            scheduling.setServiceDescription(request.getServiceDescription());
        }

        if (request.getStatus() != null) {
            try {
                SchedulingStatus newStatus = SchedulingStatus.valueOf(request.getStatus().toUpperCase());
                scheduling.setStatus(newStatus);

                // Se o status for alterado para COMPLETED, atualizar a data de conclusão
                if (newStatus == SchedulingStatus.COMPLETED) {
                    scheduling.setCompletedAt(LocalDateTime.now());
                }
            } catch (IllegalArgumentException e) {
                // Ignorar status inválido
            }
        }

        if (request.getPriority() != null) {
            try {
                scheduling.setPriority(ServicePriority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignorar prioridade inválida
            }
        }

        // Salvar as alterações do agendamento
        scheduling = schedulingRepository.save(scheduling);

        // Atualizar equipamentos, se fornecidos
        if (request.getEquipments() != null) {
            // Remover equipamentos existentes
            equipmentRepository.deleteAll(equipmentRepository.findByScheduling(scheduling));

            // Adicionar novos equipamentos
            for (EquipmentRequest equipmentRequest : request.getEquipments()) {
                Equipment equipment = new Equipment();
                equipment.setScheduling(scheduling);
                equipment.setName(equipmentRequest.getName());
                equipment.setSerialNumber(equipmentRequest.getSerialNumber());
                equipment.setDescription(equipmentRequest.getDescription());
                equipmentRepository.save(equipment);
            }
        }

        // Atualizar peças de reposição, se fornecidas
        if (request.getSpareParts() != null) {
            // Remover peças existentes
            sparePartRepository.deleteAll(sparePartRepository.findByScheduling(scheduling));

            // Adicionar novas peças
            for (SparePartRequest sparePartRequest : request.getSpareParts()) {
                SparePart sparePart = new SparePart();
                sparePart.setScheduling(scheduling);
                sparePart.setName(sparePartRequest.getName());
                sparePart.setPartNumber(sparePartRequest.getPartNumber());
                sparePart.setQuantity(sparePartRequest.getQuantity());
                sparePartRepository.save(sparePart);
            }
        }

        // Enviar notificação de atualização, se necessário
        if (request.getStatus() != null) {
            notificationService.sendSchedulingUpdatedNotification(scheduling.getId());
        }

        // Buscar o agendamento atualizado
        scheduling = schedulingRepository.findById(id).orElseThrow();

        // Retornar resposta com dados de geolocalização
        SchedulingResponse response = new SchedulingResponse(scheduling);
        enrichWithGeocodingData(response);

        return response;
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

        // Enviar notificação de exclusão
        notificationService.sendSchedulingDeletedNotification(scheduling.getId());
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
            // Lidar com falhas de geolocalização sem comprometer a resposta principal
            // Pode-se adicionar um log de erro aqui
        }
    }
}
