package com.quickbee.backend.controller;

import com.quickbee.backend.model.Address;
import com.quickbee.backend.service.AddressService;
import jakarta.validation.Valid; // <-- Make sure to import this
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses") // Using the path from your plan
public class AddressController {

    @Autowired
    private AddressService addressService;

    // POST /api/user/addresses (Create)
    @PostMapping
    public ResponseEntity<Address> addMyAddress(@Valid @RequestBody Address address) {
        // @Valid triggers the @NotBlank validation in your model
        Address savedAddress = addressService.addAddress(address);
        return ResponseEntity.ok(savedAddress);
    }

    // GET /api/user/addresses (Read All)
    @GetMapping
    public ResponseEntity<List<Address>> getMyAddresses() {
        return ResponseEntity.ok(addressService.getMyAddresses());
    }

    // GET /api/user/addresses/{id} (Read One)
    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(addressService.getAddressById(id));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build(); // 403 Forbidden
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build(); // 404 Not Found
        }
    }

    // PUT /api/user/addresses/{id} (Update)
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateMyAddress(@PathVariable String id,
                                                   @Valid @RequestBody Address addressDetails) {
        try {
            Address updatedAddress = addressService.updateAddress(id, addressDetails);
            return ResponseEntity.ok(updatedAddress);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }

    // DELETE /api/user/addresses/{id} (Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMyAddress(@PathVariable String id) {
        try {
            addressService.deleteAddress(id);
            return ResponseEntity.ok().body("Address deleted successfully");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}