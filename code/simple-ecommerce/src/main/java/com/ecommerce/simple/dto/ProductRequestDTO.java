package com.ecommerce.simple.dto;

public record ProductRequestDTO(String name,
                                String description,
                                Integer quantity,
                                Double price) {
}
