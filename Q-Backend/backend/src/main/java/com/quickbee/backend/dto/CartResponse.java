package com.quickbee.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CartResponse {
    private String cartId;
    private String userId;
    private List<CartItemResponse> items;
    private Double subtotal;
    private Integer totalItems;
}