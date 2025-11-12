package com.quickbee.backend.repository;

import com.quickbee.backend.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    // We'll need this to verify the payment
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}