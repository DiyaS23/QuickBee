package com.quickbee.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Indexed
    private String productId;

    // Snapshots (so cart is stable if product changes later)
    private String name;
    private String imageUrl;
    private Double price;   // snapshot of current price when added/updated

    private Integer qty;
}