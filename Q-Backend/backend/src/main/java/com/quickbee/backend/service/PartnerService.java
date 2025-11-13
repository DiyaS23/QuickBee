package com.quickbee.backend.service;

import com.quickbee.backend.model.User;
import com.quickbee.backend.model.enums.PartnerStatus;
import com.quickbee.backend.model.enums.Role;
import com.quickbee.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Optional;

@Service
public class PartnerService {

    private final UserRepository userRepository;

    public PartnerService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * User applies to be a delivery partner.
     * Saves driver info and marks partnerStatus = PENDING_VERIFICATION.
     */
    public User applyAsDriver(String userId,
                              String vehicleType,
                              String vehicleNumber,
                              String licenseUrl,
                              String idDocUrl,
                              Integer maxCapacity) {
        Optional<User> opt = userRepository.findById(userId);
        if (!opt.isPresent()) throw new IllegalArgumentException("Invalid userId");

        User user = opt.get();
        user.setDriver(true);
        user.setRole(Role.DELIVERY_PARTNER);
        user.setVehicleType(vehicleType);
        user.setVehicleNumber(vehicleNumber);
        user.setLicenseDocumentUrl(licenseUrl);
        user.setIdDocumentUrl(idDocUrl);
        user.setMaxCapacity(maxCapacity == null ? 1 : maxCapacity);
        user.setPartnerStatus(PartnerStatus.PENDING_VERIFICATION);
        user.setVerified(false);
        userRepository.save(user);
        return user;
    }

    /**
     * Admin verifies partner (simple version).
     * Sets partnerStatus = VERIFIED, and moves to ACTIVE (offline).
     */
    public User verifyPartnerByAdmin(String userId, boolean approve) {
        Optional<User> opt = userRepository.findById(userId);
        if (!opt.isPresent()) throw new IllegalArgumentException("Invalid userId");

        User user = opt.get();
        if (!user.isDriver()) throw new IllegalArgumentException("User has not applied as driver");

        if (approve) {
            user.setVerified(true);
            user.setPartnerStatus(PartnerStatus.VERIFIED); // or ACTIVE
            user.setOnline(false); // start offline
        } else {
            user.setVerified(false);
            user.setPartnerStatus(PartnerStatus.SUSPENDED); // or keep pending with rejection reason
        }
        return userRepository.save(user);
    }

    /**
     * Driver toggles availability. When set to true, partnerStatus becomes AVAILABLE (if verified).
     * When set to false, becomes ACTIVE or UNAVAILABLE.
     *
     * IMPORTANT: When going AVAILABLE you should call assignment logic to attempt assigning orders.
     */
    public User setAvailability(String userId, boolean available, Double lat, Double lng) {
        Optional<User> opt = userRepository.findById(userId);
        if (!opt.isPresent()) throw new IllegalArgumentException("Invalid userId");

        User user = opt.get();
        if (!user.isDriver() || !user.isVerified()) {
            throw new IllegalStateException("Driver must be verified before going available");
        }

        user.setOnline(available);
        user.setCurrentLat(lat);
        user.setCurrentLng(lng);
        user.setLastSeen(Instant.now());

        if (available) {
            user.setPartnerStatus(PartnerStatus.AVAILABLE);
        } else {
            user.setPartnerStatus(PartnerStatus.ACTIVE); // or UNAVAILABLE if break
        }
        return userRepository.save(user);
    }
}
