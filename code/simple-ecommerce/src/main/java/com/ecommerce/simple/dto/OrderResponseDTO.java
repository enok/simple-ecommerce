package com.ecommerce.simple.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OrderResponseDTO {
    private Integer id;
    private String description;
    private Double totalAmount;
}
