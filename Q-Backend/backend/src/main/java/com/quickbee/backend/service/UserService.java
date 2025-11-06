package com.quickbee.backend.service;

import com.quickbee.backend.dto.SignUpRequest;
import com.quickbee.backend.model.User;
import com.quickbee.backend.model.enums.AccountStatus;
import com.quickbee.backend.model.enums.Role;
import com.quickbee.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}