package com.jacto.scheduler.controller;

import com.jacto.scheduler.payload.request.ClientFeedbackRequest;
import com.jacto.scheduler.payload.request.SchedulingRequest;
import com.jacto.scheduler.payload.request.SchedulingUpdateRequest;
import com.jacto.scheduler.payload.response.MessageResponse;
import com.jacto.scheduler.payload.response.SchedulingResponse;
import com.jacto.scheduler.payload.response.TechnicianPerformanceResponse;
import com.jacto.scheduler.service.SchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/scheduling")
@Tag(name = "Agendamentos", description = "API para gestão de agendamentos de visitas técnicas")
@SecurityRequirement(name = "bearerAuth")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    @GetMapping
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @Operation(
        summary = "Listar todos os agendamentos",
        description = "Retorna todos os agendamentos do técnico autenticado",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de agendamentos",
                content = @Content(schema = @Schema(implementation = SchedulingResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
        }
    )
    public ResponseEntity<List<SchedulingResponse>> getAllSchedulings() {
        List<SchedulingResponse> schedulings = schedulingService.getAllSchedulingsForCurrentUser();
        return ResponseEntity.ok(schedulings);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @Operation(
        summary = "Listar agendamentos futuros",
        description = "Retorna todos os agendamentos futuros do técnico autenticado",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de agendamentos futuros",
                content = @Content(schema = @Schema(implementation = SchedulingResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
        }
    )
    public ResponseEntity<List<SchedulingResponse>> getUpcomingSchedulings() {
        List<SchedulingResponse> schedulings = schedulingService.getUpcomingSchedulings();
        return ResponseEntity.ok(schedulings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @Operation(
        summary = "Buscar agendamento por ID",
        description = "Retorna um agendamento específico pelo seu ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Agendamento encontrado",
                content = @Content(schema = @Schema(implementation = SchedulingResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
        }
    )
    public ResponseEntity<SchedulingResponse> getSchedulingById(
            @Parameter(description = "ID do agendamento") @PathVariable Long id) {
        SchedulingResponse scheduling = schedulingService.getSchedulingById(id);
        return ResponseEntity.ok(scheduling);
    }

    @PostMapping
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @Operation(
        summary = "Criar novo agendamento",
        description = "Cria um novo agendamento de visita técnica",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Agendamento criado com sucesso",
                content = @Content(schema = @Schema(implementation = SchedulingResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou data anterior à atual"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
        }
    )
    public ResponseEntity<SchedulingResponse> createScheduling(
            @Valid @RequestBody SchedulingRequest request) {
        SchedulingResponse createdScheduling = schedulingService.createScheduling(request);
        return ResponseEntity.ok(createdScheduling);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @Operation(
        summary = "Atualizar agendamento",
        description = "Atualiza um agendamento existente",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Agendamento atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = SchedulingResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou data anterior à atual"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
        }
    )
    public ResponseEntity<SchedulingResponse> updateScheduling(
            @Parameter(description = "ID do agendamento") @PathVariable Long id,
            @Valid @RequestBody SchedulingUpdateRequest request) {
        SchedulingResponse updatedScheduling = schedulingService.updateScheduling(id, request);
        return ResponseEntity.ok(updatedScheduling);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @Operation(
        summary = "Excluir agendamento",
        description = "Exclui um agendamento existente",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Agendamento excluído com sucesso",
                content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
        }
    )
    public ResponseEntity<MessageResponse> deleteScheduling(
            @Parameter(description = "ID do agendamento") @PathVariable Long id) {
        schedulingService.deleteScheduling(id);
        return ResponseEntity.ok(new MessageResponse("Agendamento excluído com sucesso"));
    }

    @PostMapping("/{id}/feedback")
    @Operation(
        summary = "Adicionar feedback do cliente",
        description = "Adiciona avaliação e feedback do cliente a um agendamento concluído",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Feedback adicionado com sucesso",
                content = @Content(schema = @Schema(implementation = SchedulingResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou agendamento não concluído"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
        }
    )
    public ResponseEntity<SchedulingResponse> addClientFeedback(
            @Parameter(description = "ID do agendamento") @PathVariable Long id,
            @Valid @RequestBody ClientFeedbackRequest request) {
        SchedulingResponse updatedScheduling = schedulingService.addClientFeedback(id, request);
        return ResponseEntity.ok(updatedScheduling);
    }

    @GetMapping("/technician/{id}/performance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obter métricas de desempenho do técnico",
        description = "Retorna métricas de desempenho para um técnico específico",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Métricas de desempenho",
                content = @Content(schema = @Schema(implementation = TechnicianPerformanceResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
        }
    )
    public ResponseEntity<TechnicianPerformanceResponse> getTechnicianPerformance(
            @Parameter(description = "ID do técnico") @PathVariable Long id) {
        TechnicianPerformanceResponse performance = schedulingService.getTechnicianPerformance(id);
        return ResponseEntity.ok(performance);
    }
}
