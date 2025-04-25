package com.jacto.scheduler.report.service;

import com.jacto.scheduler.report.dto.VisitReportDTO;
import org.springframework.core.io.Resource;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {
    Resource generateVisitsReport(LocalDateTime startDate, LocalDateTime endDate);
    Resource generateTechnicianPerformanceReport(LocalDateTime startDate, LocalDateTime endDate);
    Resource generateCustomerAnalysisReport(LocalDateTime startDate, LocalDateTime endDate);
    Resource generateEquipmentMaintenanceReport(LocalDateTime startDate, LocalDateTime endDate);
    Resource generateSLAComplianceReport(LocalDateTime startDate, LocalDateTime endDate);
}
