package com.ecommerce.simple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class OrderItemResponseDTO {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private String productDescription;
    private Double productPrice;
}
