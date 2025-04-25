package com.jacto.scheduler.payload.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TechnicianPerformanceResponse {

    private Long technicianId;
    private String technicianName;
    private Double averageRating;
    private Long completedVisits;
    private Double averageVisitDuration; // em horas

    // Construtor padrão para deserialização JSON
    public TechnicianPerformanceResponse() {
    }

    // Construtor com todos os campos
    @JsonCreator
    public TechnicianPerformanceResponse(
            @JsonProperty("technicianId") Long technicianId,
            @JsonProperty("technicianName") String technicianName,
            @JsonProperty("averageRating") Double averageRating,
            @JsonProperty("completedVisits") Long completedVisits,
            @JsonProperty("averageVisitDuration") Double averageVisitDuration) {
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.averageRating = averageRating;
        this.completedVisits = completedVisits;
        this.averageVisitDuration = averageVisitDuration;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getCompletedVisits() {
        return completedVisits;
    }

    public void setCompletedVisits(Long completedVisits) {
        this.completedVisits = completedVisits;
    }

    public Double getAverageVisitDuration() {
        return averageVisitDuration;
    }

    public void setAverageVisitDuration(Double averageVisitDuration) {
        this.averageVisitDuration = averageVisitDuration;
    }
}
