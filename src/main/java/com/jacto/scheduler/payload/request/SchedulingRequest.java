package com.jacto.scheduler.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class SchedulingRequest {

    @NotBlank
    private String farmName;

    @NotBlank(message = "O nome do cliente é obrigatório")
    private String clientName;

    @NotBlank(message = "O email do cliente é obrigatório")
    @Email(message = "Email inválido")
    private String clientEmail;

    @NotBlank(message = "O endereço é obrigatório")
    private String address;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    @FutureOrPresent(message = "A data de agendamento não pode ser anterior à data atual")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;

    private String serviceDescription;

    private String priority;

    private List<EquipmentRequest> equipments;

    private List<SparePartRequest> spareParts;

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

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<EquipmentRequest> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<EquipmentRequest> equipments) {
        this.equipments = equipments;
    }

    public List<SparePartRequest> getSpareParts() {
        return spareParts;
    }

    public void setSpareParts(List<SparePartRequest> spareParts) {
        this.spareParts = spareParts;
    }
}
