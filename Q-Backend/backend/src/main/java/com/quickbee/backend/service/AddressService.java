package com.quickbee.backend.service;

import com.quickbee.backend.model.Address;
import com.quickbee.backend.model.User;
import com.quickbee.backend.repository.AddressRepository;
import com.quickbee.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important!

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to get the currently authenticated user
    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = ((UserDetails) principal).getUsername();

        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // --- CRUD Operations ---

    public List<Address> getMyAddresses() {
        User user = getAuthenticatedUser();
        return addressRepository.findByUserId(user.getId());
    }

    public Address getAddressById(String addressId) {
        User user = getAuthenticatedUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Ownership Check
        if (!address.getUserId().equals(user.getId())) {
            throw new SecurityException("Access Denied: You do not own this address");
        }
        return address;
    }

    @Transactional // Use transaction for multi-step save
    public Address addAddress(Address address) {
        User user = getAuthenticatedUser();

        // 1. Set the owner of this address
        address.setUserId(user.getId());

        // 2. Save the new address
        Address savedAddress = addressRepository.save(address);

        // 3. Add the new address ID to the user's list (as per your schema)
        user.getAddress_details().add(savedAddress.getId());
        userRepository.save(user); // Save the updated user

        return savedAddress;
    }

    public Address updateAddress(String addressId, Address addressDetails) {
        User user = getAuthenticatedUser();

        // 1. Get the address and verify ownership
        Address existingAddress = getAddressById(addressId); // This already checks ownership

        // 2. Update the fields
        existingAddress.setName(addressDetails.getName());
        existingAddress.setPhone(addressDetails.getPhone());
        existingAddress.setLine1(addressDetails.getLine1());
        existingAddress.setLine2(addressDetails.getLine2());
        existingAddress.setCity(addressDetails.getCity());
        existingAddress.setState(addressDetails.getState());
        existingAddress.setPincode(addressDetails.getPincode());
        // (We don't update lat/lng manually)

        // 3. Save and return the updated address
        return addressRepository.save(existingAddress);
    }

    @Transactional
    public void deleteAddress(String addressId) {
        User user = getAuthenticatedUser();

        // 1. Get the address and verify ownership
        Address address = getAddressById(addressId); // This already checks ownership

        // 2. Remove the address ID from the user's list
        user.getAddress_details().remove(addressId);
        userRepository.save(user);

        // 3. Delete the address itself
        addressRepository.delete(address);
    }
}