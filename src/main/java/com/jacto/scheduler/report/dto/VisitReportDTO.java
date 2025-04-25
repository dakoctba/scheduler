package com.jacto.scheduler.report.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VisitReportDTO {
    private Long visitId;
    private String customerName;
    private String technicianName;
    private LocalDateTime scheduledDate;
    private LocalDateTime completionDate;
    private String status;
    private String equipmentName;
    private String problemDescription;
    private String solution;
    private Integer durationMinutes;
    private String customerFeedback;
}
