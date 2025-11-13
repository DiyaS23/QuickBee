package com.quickbee.backend.model;

import com.quickbee.backend.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@Document("orders")
public class Order {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String addressId;          // reference
    private AddressSnapshot address;   // immutable copy at order time

    private List<OrderItem> items;

    private Double subtotal;           // sum(priceAtPurchase * qty)
    private Double deliveryFee;        // e.g., flat or computed
    private Double total;              // subtotal + deliveryFee

    private OrderStatus status=OrderStatus.PENDING;
    // --- delivery related ---
    private String assignedPartnerId; // partner assigned (null if none)
    private Instant queuedAt;         // when order was queued for delivery
    private Instant assignedAt;       // when assigned
    private Instant acceptedAt;       // when partner accepted
    private Instant pickedAt;         // when picked up
    private Instant deliveredAt;      // when delivered

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
