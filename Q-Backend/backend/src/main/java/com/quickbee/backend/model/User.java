package com.quickbee.backend.model;

import com.quickbee.backend.model.enums.AccountStatus;
import com.quickbee.backend.model.enums.Role;
import lombok.Data; // Automatically adds Getters, Setters, etc.
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id; // Your schema's "_id"

    private String name;

    @Indexed(unique = true) // Enforces unique email
    private String email;

    private String password; // Will be stored hashed

    @Indexed(unique = true, sparse = true) // Allows nulls, but if not null, must be unique
    private String mobile;

    private Role role; // From your Enum
    private AccountStatus status; // From your Enum

    private boolean verify_email = false; // Default to false

    // We will add logic for these later. For now, they are empty.
    private List<String> address_details = new ArrayList<>(); // List of Address IDs
    private List<String> orderHistory = new ArrayList<>();    // List of Order IDs

    @CreatedDate
    private Date createdAt; // Automatically set on creation

    @LastModifiedDate
    private Date updatedAt; // Automatically set on update

    // --- Fields We Will Add in LATER Features ---
    // private String avatar; // For "Profile" feature
    // private String refresh_token; // For "Login" feature (advanced)
    // private List<String> shopping_cart; // For "Cart" feature
    // private String forgot_password_otp; // For "Forgot Password" feature
    // private Date forgot_password_expiry; // For "Forgot Password" feature
}