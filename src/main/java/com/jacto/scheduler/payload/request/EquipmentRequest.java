package com.jacto.scheduler.payload.request;

import jakarta.validation.constraints.NotBlank;

public class EquipmentRequest {

    @NotBlank
    private String name;

    private String serialNumber;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
