package com.quickbee.backend.model;

import com.quickbee.backend.model.enums.PaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "payments") // A brand new collection
public class Payment {

    @Id
    private String id;

    @Indexed
    private String orderId; // <-- This is the link to your friend's Order

    @Indexed
    private String userId;  // The user who made the payment

    private PaymentStatus status = PaymentStatus.PENDING;

    private Double amount; // The amount for this payment attempt

    // --- Razorpay Fields ---
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public Payment(String orderId, String userId, Double amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
    }
}