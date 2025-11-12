package com.quickbee.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePaymentResponse {
    private String razorpayOrderId;
    private int amount; // (in paise)
    private String keyId; // Your public Razorpay key
    private String currency = "INR";
}