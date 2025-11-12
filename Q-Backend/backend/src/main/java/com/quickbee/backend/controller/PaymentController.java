package com.quickbee.backend.controller;

import com.quickbee.backend.dto.CreatePaymentRequest;
import com.quickbee.backend.dto.CreatePaymentResponse;
import com.quickbee.backend.dto.VerifyPaymentRequest;
import com.quickbee.backend.model.Order;
import com.quickbee.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody CreatePaymentRequest request) {
        try {
            CreatePaymentResponse response = paymentService.createRazorpayOrder(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        try {
            Order order = paymentService.verifyPayment(request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}