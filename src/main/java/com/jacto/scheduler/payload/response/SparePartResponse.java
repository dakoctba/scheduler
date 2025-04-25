package com.jacto.scheduler.payload.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jacto.scheduler.model.SparePart;

public class SparePartResponse {

    private Long id;
    private String name;
    private String partNumber;
    private Integer quantity;

    // Construtor padrão para deserialização JSON
    public SparePartResponse() {
    }

    // Construtor com base na entidade SparePart
    public SparePartResponse(SparePart sparePart) {
        this.id = sparePart.getId();
        this.name = sparePart.getName();
        this.partNumber = sparePart.getPartNumber();
        this.quantity = sparePart.getQuantity();
    }

    // Construtor para deserialização JSON
    @JsonCreator
    public SparePartResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("partNumber") String partNumber,
            @JsonProperty("quantity") Integer quantity) {
        this.id = id;
        this.name = name;
        this.partNumber = partNumber;
        this.quantity = quantity;
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

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
