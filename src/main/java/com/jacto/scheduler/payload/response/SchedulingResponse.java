package com.jacto.scheduler.payload.response;

import com.jacto.scheduler.model.Scheduling;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SchedulingResponse {

    private Long id;

    private String farmName;
    private String clientName;
    private String clientEmail;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDateTime scheduledAt;
    private LocalDateTime completedAt;
    private String serviceDescription;
    private String status;
    private String priority;
    private Double clientRating;
    private String clientFeedback;
    private List<EquipmentResponse> equipments;
    private List<SparePartResponse> spareParts;
    private GeoLocationDetails locationDetails;

    private String technicianUsername;
    private String technicianFullName;
    private String technicianEmail;
    private Long technicianId;

    // Construtor padrão para deserialização JSON
    public SchedulingResponse() {
    }

    // Construtor com base na entidade Scheduling
    public SchedulingResponse(Scheduling scheduling) {
        this.id = scheduling.getId();

        this.farmName = scheduling.getFarmName();
        this.clientName = scheduling.getClientName();
        this.clientEmail = scheduling.getClientEmail();
        this.address = scheduling.getAddress();
        this.latitude = scheduling.getLatitude();
        this.longitude = scheduling.getLongitude();
        this.scheduledAt = scheduling.getScheduledAt();
        this.completedAt = scheduling.getCompletedAt();
        this.serviceDescription = scheduling.getServiceDescription();
        this.status = scheduling.getStatus().name();
        this.priority = scheduling.getPriority().name();
        this.clientRating = scheduling.getClientRating();
        this.clientFeedback = scheduling.getClientFeedback();

        this.technicianUsername = scheduling.getTechnician().getUsername();
        this.technicianFullName = scheduling.getTechnician().getFullName();
        this.technicianEmail = scheduling.getTechnician().getEmail();
        this.technicianId = scheduling.getTechnician().getId();

        this.equipments = scheduling.getEquipments().stream()
                .map(EquipmentResponse::new)
                .collect(Collectors.toList());

        this.spareParts = scheduling.getSpareParts().stream()
                .map(SparePartResponse::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTechnicianUsername() {
        return technicianUsername;
    }

    public void setTechnicianUsername(String technicianUsername) {
        this.technicianUsername = technicianUsername;
    }

    public String getTechnicianFullName() {
        return technicianFullName;
    }

    public void setTechnicianFullName(String technicianFullName) {
        this.technicianFullName = technicianFullName;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Double getClientRating() {
        return clientRating;
    }

    public void setClientRating(Double clientRating) {
        this.clientRating = clientRating;
    }

    public String getClientFeedback() {
        return clientFeedback;
    }

    public void setClientFeedback(String clientFeedback) {
        this.clientFeedback = clientFeedback;
    }

    public List<EquipmentResponse> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<EquipmentResponse> equipments) {
        this.equipments = equipments;
    }

    public List<SparePartResponse> getSpareParts() {
        return spareParts;
    }

    public void setSpareParts(List<SparePartResponse> spareParts) {
        this.spareParts = spareParts;
    }

    public GeoLocationDetails getLocationDetails() {
        return locationDetails;
    }

    public void setLocationDetails(GeoLocationDetails locationDetails) {
        this.locationDetails = locationDetails;
    }

    public String getTechnicianEmail() {
        return technicianEmail;
    }

    public void setTechnicianEmail(String technicianEmail) {
        this.technicianEmail = technicianEmail;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }
}
