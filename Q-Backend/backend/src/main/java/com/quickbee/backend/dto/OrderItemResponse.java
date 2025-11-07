package com.quickbee.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class OrderItemResponse {
    private String productId;
    private String name;
    private String imageUrl;
    private Double priceAtPurchase;
    private Integer qty;
    private Double lineTotal;
}
