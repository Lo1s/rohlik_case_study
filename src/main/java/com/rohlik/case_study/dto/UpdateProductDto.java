package com.rohlik.case_study.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UpdateProductDto {
    @NotBlank
    private String name;

    @NotNull @Min(0)
    private Integer quantity;

    @NotNull @Min(0)
    private Double price;

    // Getters and setters

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}