package com.quickbee.backend.repository;

import com.quickbee.backend.model.DeliveryAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DeliveryAssignmentRepository extends MongoRepository<DeliveryAssignment, String> {
    List<DeliveryAssignment> findByPartnerIdAndStatus(String partnerId, com.quickbee.backend.model.enums.AssignmentStatus status);
}
