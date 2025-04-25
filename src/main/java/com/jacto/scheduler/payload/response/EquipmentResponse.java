package com.jacto.scheduler.payload.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jacto.scheduler.model.Equipment;

public class EquipmentResponse {

    private Long id;
    private String name;
    private String serialNumber;
    private String description;

    // Construtor padrão para deserialização JSON
    public EquipmentResponse() {
    }

    // Construtor com base na entidade Equipment
    public EquipmentResponse(Equipment equipment) {
        this.id = equipment.getId();
        this.name = equipment.getName();
        this.serialNumber = equipment.getSerialNumber();
        this.description = equipment.getDescription();
    }

    // Construtor para deserialização JSON
    @JsonCreator
    public EquipmentResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("serialNumber") String serialNumber,
            @JsonProperty("description") String description) {
        this.id = id;
        this.name = name;
        this.serialNumber = serialNumber;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
