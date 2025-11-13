package com.quickbee.backend.model;

import com.quickbee.backend.model.enums.AssignmentStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "delivery_assignments")
public class DeliveryAssignment {
    @Id
    private String id;
    private String orderId;
    private String partnerId;
    private AssignmentStatus status;
    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant pickedAt;
    private Instant completedAt;
    private Integer attemptCount = 0; // how many times this order was re-assigned
    private String notes;
}
