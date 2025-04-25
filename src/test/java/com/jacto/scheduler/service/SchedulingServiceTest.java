package com.jacto.scheduler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.jacto.scheduler.enumerations.SchedulingStatus;
import com.jacto.scheduler.enumerations.ServicePriority;
import com.jacto.scheduler.exception.ResourceNotFoundException;
import com.jacto.scheduler.model.Scheduling;
import com.jacto.scheduler.model.User;
import com.jacto.scheduler.payload.request.ClientFeedbackRequest;
import com.jacto.scheduler.payload.request.SchedulingRequest;
import com.jacto.scheduler.payload.request.SchedulingUpdateRequest;
import com.jacto.scheduler.payload.response.GeoLocationDetails;
import com.jacto.scheduler.payload.response.SchedulingResponse;
import com.jacto.scheduler.repository.EquipmentRepository;
import com.jacto.scheduler.repository.SchedulingRepository;
import com.jacto.scheduler.repository.SparePartRepository;
import com.jacto.scheduler.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchedulingServiceTest {

    @Mock
    private SchedulingRepository schedulingRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private SparePartRepository sparePartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private RedisSchedulingService redisSchedulingService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SchedulingService schedulingService;

    private User testUser;
    private Scheduling testScheduling;
    private SchedulingRequest testSchedulingRequest;

    @BeforeEach
    void setUp() {
        // Configurar usuário de teste
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Configurar agendamento de teste
        testScheduling = new Scheduling();
        testScheduling.setId(1L);
        testScheduling.setTechnician(testUser);
        testScheduling.setFarmName("Fazenda Teste");
        testScheduling.setClientName("Cliente Teste");
        testScheduling.setClientEmail("cliente@teste.com");
        testScheduling.setAddress("Endereço Teste");
        testScheduling.setLatitude(-23.5505);
        testScheduling.setLongitude(-46.6333);
        testScheduling.setScheduledAt(LocalDateTime.now().plusDays(1));
        testScheduling.setServiceDescription("Serviço de teste");
        testScheduling.setStatus(SchedulingStatus.PENDING);
        testScheduling.setPriority(ServicePriority.MEDIUM);

        // Configurar request de teste
        testSchedulingRequest = new SchedulingRequest();
        testSchedulingRequest.setFarmName("Fazenda Teste");
        testSchedulingRequest.setClientName("Cliente Teste");
        testSchedulingRequest.setClientEmail("cliente@teste.com");
        testSchedulingRequest.setAddress("Endereço Teste");
        testSchedulingRequest.setLatitude(-23.5505);
        testSchedulingRequest.setLongitude(-46.6333);
        testSchedulingRequest.setScheduledAt(LocalDateTime.now().plusDays(1));
        testSchedulingRequest.setServiceDescription("Serviço de teste");
        testSchedulingRequest.setPriority("MEDIUM");

        // Configurar contexto de segurança
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void createScheduling_ShouldCreateNewScheduling() {
        // Arrange
        when(schedulingRepository.save(any(Scheduling.class))).thenReturn(testScheduling);
        when(schedulingRepository.findById(anyLong())).thenReturn(Optional.of(testScheduling));
        when(geocodingService.getLocationDetails(anyDouble(), anyDouble()))
                .thenReturn(new GeoLocationDetails());

        // Act
        SchedulingResponse response = schedulingService.createScheduling(testSchedulingRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testSchedulingRequest.getFarmName(), response.getFarmName());
        assertEquals(testSchedulingRequest.getClientName(), response.getClientName());
        verify(schedulingRepository).save(any(Scheduling.class));
        verify(notificationService).sendSchedulingCreatedNotification(anyLong());
    }

    @Test
    void createScheduling_WithPastDate_ShouldThrowException() {
        // Arrange
        testSchedulingRequest.setScheduledAt(LocalDateTime.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            schedulingService.createScheduling(testSchedulingRequest)
        );
    }

    @Test
    void getSchedulingById_ShouldReturnScheduling() {
        // Arrange
        when(schedulingRepository.findById(1L)).thenReturn(Optional.of(testScheduling));
        when(geocodingService.getLocationDetails(anyDouble(), anyDouble()))
                .thenReturn(new GeoLocationDetails());

        // Act
        SchedulingResponse response = schedulingService.getSchedulingById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(testScheduling.getId(), response.getId());
        assertEquals(testScheduling.getFarmName(), response.getFarmName());
    }

    @Test
    void getSchedulingById_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(schedulingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            schedulingService.getSchedulingById(999L)
        );
    }

    @Test
    void updateScheduling_ShouldUpdateExistingScheduling() {
        // Arrange
        SchedulingUpdateRequest updateRequest = new SchedulingUpdateRequest();
        updateRequest.setFarmName("Nova Fazenda");
        updateRequest.setStatus("COMPLETED");

        when(schedulingRepository.findById(1L)).thenReturn(Optional.of(testScheduling));
        when(schedulingRepository.save(any(Scheduling.class))).thenReturn(testScheduling);
        when(geocodingService.getLocationDetails(anyDouble(), anyDouble()))
                .thenReturn(new GeoLocationDetails());

        // Act
        SchedulingResponse response = schedulingService.updateScheduling(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Nova Fazenda", response.getFarmName());
        assertEquals("COMPLETED", response.getStatus());
        verify(schedulingRepository).save(any(Scheduling.class));
        verify(notificationService).sendSchedulingUpdatedNotification(anyLong());
    }

    @Test
    void deleteScheduling_ShouldDeleteExistingScheduling() {
        // Arrange
        when(schedulingRepository.findById(1L)).thenReturn(Optional.of(testScheduling));

        // Act
        schedulingService.deleteScheduling(1L);

        // Assert
        verify(schedulingRepository).delete(testScheduling);
        verify(redisSchedulingService).deleteScheduling(1L);
    }

    @Test
    void addClientFeedback_ShouldAddFeedbackToCompletedScheduling() {
        // Arrange
        testScheduling.setStatus(SchedulingStatus.COMPLETED);
        ClientFeedbackRequest feedbackRequest = new ClientFeedbackRequest();
        feedbackRequest.setRating(5.0);
        feedbackRequest.setFeedback("Ótimo serviço!");

        when(schedulingRepository.findById(1L)).thenReturn(Optional.of(testScheduling));
        when(schedulingRepository.save(any(Scheduling.class))).thenReturn(testScheduling);
        when(geocodingService.getLocationDetails(anyDouble(), anyDouble()))
                .thenReturn(new GeoLocationDetails());

        // Act
        SchedulingResponse response = schedulingService.addClientFeedback(1L, feedbackRequest);

        // Assert
        assertNotNull(response);
        assertEquals(5.0, response.getClientRating());
        assertEquals("Ótimo serviço!", response.getClientFeedback());
    }

    @Test
    void addClientFeedback_ToNonCompletedScheduling_ShouldThrowException() {
        // Arrange
        ClientFeedbackRequest feedbackRequest = new ClientFeedbackRequest();
        feedbackRequest.setRating(5.0);
        feedbackRequest.setFeedback("Ótimo serviço!");

        when(schedulingRepository.findById(1L)).thenReturn(Optional.of(testScheduling));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
            schedulingService.addClientFeedback(1L, feedbackRequest)
        );
    }
}
