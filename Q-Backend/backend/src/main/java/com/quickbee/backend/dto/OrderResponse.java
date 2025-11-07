package com.quickbee.backend.dto;

import com.quickbee.backend.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data @AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private OrderStatus status;
    private Double subtotal;
    private Double deliveryFee;
    private Double total;
    private List<OrderItemResponse> items;
    private AddressDto address;
    private Instant createdAt;

    @Data @AllArgsConstructor
    public static class AddressDto {
        private String name;
        private String phone;
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String pincode;
    }
}
