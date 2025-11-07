package com.quickbee.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderItem {
    private String productId;
    private String name;           // snapshot
    private String imageUrl;       // snapshot
    private Double priceAtPurchase;
    private Integer qty;
}
