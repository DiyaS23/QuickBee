package com.quickbee.backend.dto;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    // The ID of the order in *our* database
    private String ourOrderId;
}