package com.ecommerce.simple.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ProductResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer quantity;
    private Double price;
}
