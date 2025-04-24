package com.jacto.scheduler.payload.response;

import com.jacto.scheduler.model.SparePart;

public class SparePartResponse {

    private Long id;
    private String name;
    private String partNumber;
    private Integer quantity;

    public SparePartResponse(SparePart sparePart) {
        this.id = sparePart.getId();
        this.name = sparePart.getName();
        this.partNumber = sparePart.getPartNumber();
        this.quantity = sparePart.getQuantity();
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
