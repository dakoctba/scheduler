package com.jacto.scheduler.report.controller;

import com.jacto.scheduler.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios em Excel")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Operation(
        summary = "Gerar relatório de visitas",
        description = "Gera um relatório em Excel com detalhes de todas as visitas em um período específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Relatório gerado com sucesso",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        ),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/visits")
    public ResponseEntity<Resource> getVisitsReport(
            @Parameter(description = "Data inicial do período (formato ISO 8601)", example = "2025-04-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Data final do período (formato ISO 8601)", example = "2025-05-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Resource resource = reportService.generateVisitsReport(startDate, endDate);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=visits-report.xlsx")
                .body(resource);
    }

    @Operation(
        summary = "Gerar relatório de desempenho dos técnicos",
        description = "Gera um relatório em Excel com métricas de desempenho dos técnicos em um período específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Relatório gerado com sucesso",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        ),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/technicians/performance")
    public ResponseEntity<Resource> getTechnicianPerformanceReport(
            @Parameter(description = "Data inicial do período (formato ISO 8601)", example = "2025-04-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Data final do período (formato ISO 8601)", example = "2025-05-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Resource resource = reportService.generateTechnicianPerformanceReport(startDate, endDate);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=technician-performance-report.xlsx")
                .body(resource);
    }

    @Operation(
        summary = "Gerar relatório de análise de clientes",
        description = "Gera um relatório em Excel com métricas de análise dos clientes em um período específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Relatório gerado com sucesso",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        ),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/customers/analysis")
    public ResponseEntity<Resource> getCustomerAnalysisReport(
            @Parameter(description = "Data inicial do período (formato ISO 8601)", example = "2025-04-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Data final do período (formato ISO 8601)", example = "2025-05-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Resource resource = reportService.generateCustomerAnalysisReport(startDate, endDate);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customer-analysis-report.xlsx")
                .body(resource);
    }

    @Operation(
        summary = "Gerar relatório de manutenção de equipamentos",
        description = "Gera um relatório em Excel com métricas de manutenção dos equipamentos em um período específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Relatório gerado com sucesso",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        ),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/equipment/maintenance")
    public ResponseEntity<Resource> getEquipmentMaintenanceReport(
            @Parameter(description = "Data inicial do período (formato ISO 8601)", example = "2025-04-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Data final do período (formato ISO 8601)", example = "2025-05-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Resource resource = reportService.generateEquipmentMaintenanceReport(startDate, endDate);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=equipment-maintenance-report.xlsx")
                .body(resource);
    }

    @Operation(
        summary = "Gerar relatório de conformidade SLA",
        description = "Gera um relatório em Excel com análise de conformidade com SLA em um período específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Relatório gerado com sucesso",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        ),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/sla/compliance")
    public ResponseEntity<Resource> getSLAComplianceReport(
            @Parameter(description = "Data inicial do período (formato ISO 8601)", example = "2025-04-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Data final do período (formato ISO 8601)", example = "2025-05-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Resource resource = reportService.generateSLAComplianceReport(startDate, endDate);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sla-compliance-report.xlsx")
                .body(resource);
    }
}
