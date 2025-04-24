package com.jacto.scheduler.payload.response;

public class TechnicianPerformanceResponse {

    private Long technicianId;
    private String technicianName;
    private Double averageRating;
    private Long completedVisits;
    private Double averageVisitDuration; // em horas

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
