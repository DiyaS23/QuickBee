package com.quickbee.backend.controller;

import com.quickbee.backend.dto.JwtResponse; // <-- ADD THIS
import com.quickbee.backend.dto.LoginRequest;  // <-- ADD THIS
import com.quickbee.backend.dto.SignUpRequest; // <-- You already had this
import com.quickbee.backend.model.User;
import com.quickbee.backend.service.UserService;
import com.quickbee.backend.util.JwtUtil; // <-- ADD THIS
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // <-- ADD THIS
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // <-- ADD THIS
import org.springframework.security.core.userdetails.UserDetails; // <-- ADD THIS
import org.springframework.security.core.userdetails.UserDetailsService; // <-- ADD THIS
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // --- ADD THESE NEW FIELDS ---
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;
    // --- END OF NEW FIELDS ---

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        // ... (your existing register code is unchanged) ...
        try {
            User registeredUser = userService.registerUser(signUpRequest);
            registeredUser.setPassword(null);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- ADD THIS NEW METHOD ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Authenticate user (checks email and password)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            // If authentication fails
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        // 2. If authentication is successful, get UserDetails
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(loginRequest.getEmail());

        // 3. Generate JWT Token
        final String token = jwtUtil.generateToken(userDetails);

        // 4. Send token back
        return ResponseEntity.ok(new JwtResponse(token));
    }
}