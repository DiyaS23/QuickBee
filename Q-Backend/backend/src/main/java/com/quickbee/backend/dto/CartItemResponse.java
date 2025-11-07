package com.quickbee.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemResponse {
    private String productId;
    private String name;
    private String imageUrl;
    private Double price;
    private Integer qty;
    private Double lineTotal; // price * qty
}

