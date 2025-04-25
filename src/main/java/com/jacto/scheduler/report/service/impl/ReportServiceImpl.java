package com.jacto.scheduler.report.service.impl;

import com.jacto.scheduler.model.Scheduling;
import com.jacto.scheduler.model.User;
import com.jacto.scheduler.model.Equipment;
import com.jacto.scheduler.enumerations.SchedulingStatus;
import com.jacto.scheduler.enumerations.ServicePriority;
import com.jacto.scheduler.report.service.ReportService;
import com.jacto.scheduler.repository.SchedulingRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.AbstractMap;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private SchedulingRepository schedulingRepository;

    @Override
    public Resource generateVisitsReport(LocalDateTime startDate, LocalDateTime endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Relatório de Visitas");

            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Cliente", "Técnico", "Data Agendada", "Data Conclusão",
                              "Status", "Equipamento", "Problema", "Solução", "Duração (min)", "Feedback"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Buscar dados do banco
            List<Scheduling> schedulings = schedulingRepository.findByDateRange(startDate, endDate);

            // Preencher dados
            int rowNum = 1;
            for (Scheduling scheduling : schedulings) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(scheduling.getId());
                row.createCell(1).setCellValue(scheduling.getClientName());
                row.createCell(2).setCellValue(scheduling.getTechnician().getFullName());
                row.createCell(3).setCellValue(scheduling.getScheduledAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                row.createCell(4).setCellValue(scheduling.getCompletedAt() != null ?
                    scheduling.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
                row.createCell(5).setCellValue(scheduling.getStatus().toString());

                // Equipamentos
                StringBuilder equipments = new StringBuilder();
                scheduling.getEquipments().forEach(e -> equipments.append(e.getName()).append(", "));
                row.createCell(6).setCellValue(equipments.toString());

                row.createCell(7).setCellValue(scheduling.getServiceDescription());
                row.createCell(8).setCellValue(""); // Solução

                // Duração
                if (scheduling.getCompletedAt() != null) {
                    long durationMinutes = java.time.Duration.between(
                        scheduling.getScheduledAt(),
                        scheduling.getCompletedAt()
                    ).toMinutes();
                    row.createCell(9).setCellValue(durationMinutes);
                }

                row.createCell(10).setCellValue(scheduling.getClientFeedback());
            }

            // Estilizar cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.getCell(i);
                cell.setCellStyle(headerStyle);
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de visitas", e);
        }
    }

    @Override
    public Resource generateTechnicianPerformanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Desempenho dos Técnicos");

            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Técnico", "Total de Visitas", "Visitas Concluídas", "Visitas Pendentes",
                              "Tempo Médio de Atendimento (min)", "Satisfação Média", "Taxa de Conclusão"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Buscar dados do banco
            List<Scheduling> schedulings = schedulingRepository.findByDateRange(startDate, endDate);

            // Agrupar por técnico
            Map<User, List<Scheduling>> technicianSchedulings = schedulings.stream()
                .collect(Collectors.groupingBy(Scheduling::getTechnician));

            // Preencher dados
            int rowNum = 1;
            for (Map.Entry<User, List<Scheduling>> entry : technicianSchedulings.entrySet()) {
                User technician = entry.getKey();
                List<Scheduling> techSchedulings = entry.getValue();

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(technician.getFullName());
                row.createCell(1).setCellValue(techSchedulings.size());

                long completedVisits = techSchedulings.stream()
                    .filter(s -> s.getStatus() == SchedulingStatus.COMPLETED)
                    .count();
                row.createCell(2).setCellValue(completedVisits);

                long pendingVisits = techSchedulings.stream()
                    .filter(s -> s.getStatus() == SchedulingStatus.PENDING)
                    .count();
                row.createCell(3).setCellValue(pendingVisits);

                // Tempo médio de atendimento
                double avgDuration = techSchedulings.stream()
                    .filter(s -> s.getCompletedAt() != null)
                    .mapToLong(s -> java.time.Duration.between(s.getScheduledAt(), s.getCompletedAt()).toMinutes())
                    .average()
                    .orElse(0.0);
                row.createCell(4).setCellValue(avgDuration);

                // Satisfação média
                double avgRating = techSchedulings.stream()
                    .filter(s -> s.getClientRating() != null)
                    .mapToDouble(Scheduling::getClientRating)
                    .average()
                    .orElse(0.0);
                row.createCell(5).setCellValue(avgRating);

                // Taxa de conclusão
                double completionRate = techSchedulings.isEmpty() ? 0.0 :
                    (double) completedVisits / techSchedulings.size() * 100;
                row.createCell(6).setCellValue(completionRate);
            }

            // Estilizar cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.getCell(i);
                cell.setCellStyle(headerStyle);
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de desempenho dos técnicos", e);
        }
    }

    @Override
    public Resource generateCustomerAnalysisReport(LocalDateTime startDate, LocalDateTime endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Análise de Clientes");

            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Cliente", "Total de Visitas", "Visitas Concluídas", "Visitas Pendentes",
                              "Tempo Médio entre Visitas (dias)", "Satisfação Média", "Equipamentos Atendidos"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Buscar dados do banco
            List<Scheduling> schedulings = schedulingRepository.findByDateRange(startDate, endDate);

            // Agrupar por cliente
            Map<String, List<Scheduling>> customerSchedulings = schedulings.stream()
                .collect(Collectors.groupingBy(Scheduling::getClientName));

            // Preencher dados
            int rowNum = 1;
            for (Map.Entry<String, List<Scheduling>> entry : customerSchedulings.entrySet()) {
                String customerName = entry.getKey();
                List<Scheduling> customerVisits = entry.getValue();

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(customerName);
                row.createCell(1).setCellValue(customerVisits.size());

                long completedVisits = customerVisits.stream()
                    .filter(s -> s.getStatus() == SchedulingStatus.COMPLETED)
                    .count();
                row.createCell(2).setCellValue(completedVisits);

                long pendingVisits = customerVisits.stream()
                    .filter(s -> s.getStatus() == SchedulingStatus.PENDING)
                    .count();
                row.createCell(3).setCellValue(pendingVisits);

                // Tempo médio entre visitas
                double avgDaysBetweenVisits = 0.0;
                if (customerVisits.size() > 1) {
                    List<LocalDateTime> sortedDates = customerVisits.stream()
                        .map(Scheduling::getScheduledAt)
                        .sorted()
                        .collect(Collectors.toList());

                    long totalDays = 0;
                    for (int i = 1; i < sortedDates.size(); i++) {
                        totalDays += java.time.Duration.between(sortedDates.get(i-1), sortedDates.get(i)).toDays();
                    }
                    avgDaysBetweenVisits = (double) totalDays / (sortedDates.size() - 1);
                }
                row.createCell(4).setCellValue(avgDaysBetweenVisits);

                // Satisfação média
                double avgRating = customerVisits.stream()
                    .filter(s -> s.getClientRating() != null)
                    .mapToDouble(Scheduling::getClientRating)
                    .average()
                    .orElse(0.0);
                row.createCell(5).setCellValue(avgRating);

                // Equipamentos atendidos
                Set<String> equipments = customerVisits.stream()
                    .flatMap(s -> s.getEquipments().stream())
                    .map(Equipment::getName)
                    .collect(Collectors.toSet());
                row.createCell(6).setCellValue(String.join(", ", equipments));
            }

            // Estilizar cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.getCell(i);
                cell.setCellStyle(headerStyle);
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de análise de clientes", e);
        }
    }

    @Override
    public Resource generateEquipmentMaintenanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Manutenção de Equipamentos");

            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Equipamento", "Total de Visitas", "Visitas Concluídas", "Visitas Pendentes",
                              "Tempo Médio entre Manutenções (dias)", "Problemas Mais Frequentes", "Status Atual"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Buscar dados do banco
            List<Scheduling> schedulings = schedulingRepository.findByDateRange(startDate, endDate);

            // Agrupar por equipamento
            Map<String, List<Scheduling>> equipmentSchedulings = schedulings.stream()
                .flatMap(s -> s.getEquipments().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getName(), s)))
                .collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

            // Preencher dados
            int rowNum = 1;
            for (Map.Entry<String, List<Scheduling>> entry : equipmentSchedulings.entrySet()) {
                String equipmentName = entry.getKey();
                List<Scheduling> equipmentVisits = entry.getValue();

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(equipmentName);
                row.createCell(1).setCellValue(equipmentVisits.size());

                long completedVisits = equipmentVisits.stream()
                    .filter(s -> s.getStatus() == SchedulingStatus.COMPLETED)
                    .count();
                row.createCell(2).setCellValue(completedVisits);

                long pendingVisits = equipmentVisits.stream()
                    .filter(s -> s.getStatus() == SchedulingStatus.PENDING)
                    .count();
                row.createCell(3).setCellValue(pendingVisits);

                // Tempo médio entre manutenções
                double avgDaysBetweenMaintenance = 0.0;
                if (equipmentVisits.size() > 1) {
                    List<LocalDateTime> sortedDates = equipmentVisits.stream()
                        .map(Scheduling::getScheduledAt)
                        .sorted()
                        .collect(Collectors.toList());

                    long totalDays = 0;
                    for (int i = 1; i < sortedDates.size(); i++) {
                        totalDays += java.time.Duration.between(sortedDates.get(i-1), sortedDates.get(i)).toDays();
                    }
                    avgDaysBetweenMaintenance = (double) totalDays / (sortedDates.size() - 1);
                }
                row.createCell(4).setCellValue(avgDaysBetweenMaintenance);

                // Problemas mais frequentes
                String commonProblems = equipmentVisits.stream()
                    .map(Scheduling::getServiceDescription)
                    .filter(desc -> desc != null && !desc.isEmpty())
                    .collect(Collectors.joining(", "));
                row.createCell(5).setCellValue(commonProblems);

                // Status atual
                String currentStatus = equipmentVisits.stream()
                    .max(java.util.Comparator.comparing(Scheduling::getScheduledAt))
                    .map(s -> s.getStatus().toString())
                    .orElse("N/A");
                row.createCell(6).setCellValue(currentStatus);
            }

            // Estilizar cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.getCell(i);
                cell.setCellStyle(headerStyle);
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de manutenção de equipamentos", e);
        }
    }

    @Override
    public Resource generateSLAComplianceReport(LocalDateTime startDate, LocalDateTime endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Conformidade SLA");

            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Cliente", "Técnico", "Data Agendada", "Data Conclusão",
                              "Tempo de Resposta (min)", "Tempo de Resolução (min)", "SLA Atendido",
                              "Prioridade", "Status"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Buscar dados do banco
            List<Scheduling> schedulings = schedulingRepository.findByDateRange(startDate, endDate);

            // Preencher dados
            int rowNum = 1;
            for (Scheduling scheduling : schedulings) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(scheduling.getId());
                row.createCell(1).setCellValue(scheduling.getClientName());
                row.createCell(2).setCellValue(scheduling.getTechnician().getFullName());
                row.createCell(3).setCellValue(scheduling.getScheduledAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                row.createCell(4).setCellValue(scheduling.getCompletedAt() != null ?
                    scheduling.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");

                // Tempo de resposta (tempo entre agendamento e início do atendimento)
                long responseTime = java.time.Duration.between(
                    scheduling.getScheduledAt(),
                    scheduling.getCompletedAt() != null ? scheduling.getCompletedAt() : LocalDateTime.now()
                ).toMinutes();
                row.createCell(5).setCellValue(responseTime);

                // Tempo de resolução (tempo entre início e fim do atendimento)
                long resolutionTime = 0;
                if (scheduling.getCompletedAt() != null) {
                    resolutionTime = java.time.Duration.between(
                        scheduling.getScheduledAt(),
                        scheduling.getCompletedAt()
                    ).toMinutes();
                }
                row.createCell(6).setCellValue(resolutionTime);

                // Verificar se SLA foi atendido
                boolean slaMet = false;
                switch (scheduling.getPriority()) {
                    case HIGH:
                        slaMet = resolutionTime <= 120; // 2 horas
                        break;
                    case MEDIUM:
                        slaMet = resolutionTime <= 240; // 4 horas
                        break;
                    case LOW:
                        slaMet = resolutionTime <= 480; // 8 horas
                        break;
                }
                row.createCell(7).setCellValue(slaMet ? "Sim" : "Não");

                row.createCell(8).setCellValue(scheduling.getPriority().toString());
                row.createCell(9).setCellValue(scheduling.getStatus().toString());
            }

            // Estilizar cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.getCell(i);
                cell.setCellStyle(headerStyle);
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de conformidade SLA", e);
        }
    }
}
