package com.quickbee.backend.service;

import com.quickbee.backend.dto.SignUpRequest;
import com.quickbee.backend.model.DeliveryAssignment;
import com.quickbee.backend.model.User;
import com.quickbee.backend.model.enums.AccountStatus;
import com.quickbee.backend.model.enums.PartnerStatus;
import com.quickbee.backend.model.enums.Role;
import com.quickbee.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(SignUpRequest signUpRequest) {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account from the DTO
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setMobile(signUpRequest.getMobile());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        // Set defaults from your schema
        user.setRole(Role.USER);
        user.setStatus(AccountStatus.ACTIVE); // Or INACTIVE if you want email verification
        user.setVerify_email(false);

        return userRepository.save(user);
    }
    /**
     * Send assignment notification to partner.
     * Replace the body with your FCM/push logic.
     */
    public void notifyPartnerNewAssignment(String partnerId, DeliveryAssignment assignment) {
        Optional<User> opt = userRepository.findById(partnerId);
        if (!opt.isPresent()) {
            log.warn("notifyPartnerNewAssignment: partner not found {}", partnerId);
            return;
        }
        User partner = opt.get(); //TODO: call your push service (FCM/APNs) with payload including assignment.getId() and order details
        log.info("Notify partner {} about new assignment {}", partner.getId(), assignment.getId());
    }

    /**
     * Mark partner as free (available) after rejection/timeouts/completion.
     * This toggles partner online=true and partnerStatus to AVAILABLE.
     */
    public void freePartner(String partnerId) {
        Optional<User> opt = userRepository.findById(partnerId);
        if (!opt.isPresent()) {
            log.warn("freePartner: partner not found {}", partnerId);
            return;
        }
        User partner = opt.get();
        partner.setOnline(true);
        partner.setPartnerStatus(PartnerStatus.AVAILABLE);
        userRepository.save(partner);
        log.info("Partner {} freed and set to AVAILABLE", partnerId);
    }
    /**
     * Mark partner as busy (e.g., after assignment). Sets online=false and partnerStatus=ON_DELIVERY.
     */
    public void markPartnerBusy(String partnerId) {
        Optional<User> opt = userRepository.findById(partnerId);
        if (!opt.isPresent()) {
            log.warn("markPartnerBusy: partner not found {}", partnerId);
            return;
        }
        User partner = opt.get();
        partner.setOnline(false);
        partner.setPartnerStatus(PartnerStatus.ON_DELIVERY);
        userRepository.save(partner);
        log.info("Partner {} marked as ON_DELIVERY (busy)", partnerId);
    }

    /**
     * Find partners currently online & available (simple pool).
     * Optionally you can add location filtering here.
     */
    public List<User> findAvailablePartners() {
        return userRepository.findByIsDriverTrueAndVerifiedTrueAndOnlineTrue();
    }

    // Optionally add helper to mark partner offline:
    public void setPartnerOffline(String partnerId) {
        Optional<User> opt = userRepository.findById(partnerId);
        if (!opt.isPresent()) return;
        User p = opt.get();
        p.setOnline(false);
        p.setPartnerStatus(PartnerStatus.ACTIVE);
        userRepository.save(p);
    }
}