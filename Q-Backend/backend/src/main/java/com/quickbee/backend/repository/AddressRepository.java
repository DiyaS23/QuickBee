package com.quickbee.backend.repository;

import com.quickbee.backend.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {

    // Finds all addresses owned by a specific user
    List<Address> findByUserId(String userId);
}