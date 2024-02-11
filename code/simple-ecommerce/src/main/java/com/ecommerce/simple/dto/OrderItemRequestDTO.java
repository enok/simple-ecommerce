package com.ecommerce.simple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderItemRequestDTO {
    private Integer orderId;
    private Integer productId;
}
