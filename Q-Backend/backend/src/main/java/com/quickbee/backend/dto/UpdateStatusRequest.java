package com.quickbee.backend.dto;

import com.quickbee.backend.model.enums.OrderStatus;
import lombok.Data;

// This DTO is for your Admin Controller
@Data
public class UpdateStatusRequest {
    private OrderStatus status;
}