package com.quickbee.backend.model;

import com.quickbee.backend.model.enums.AccountStatus;
import com.quickbee.backend.model.enums.PartnerStatus;
import com.quickbee.backend.model.enums.Role;
import lombok.Data; // Automatically adds Getters, Setters, etc.
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
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

    private Role role= Role.USER; // From your Enum
    private AccountStatus status= AccountStatus.ACTIVE; // From your Enum

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

    // ---------- Delivery partner (driver) fields ----------
    // These fields are optional and only relevant when role == DELIVERY_PARTNER

    private boolean isDriver = false;         // quick flag: user opted to be a driver
    private PartnerStatus partnerStatus;      // current verification / availability status

    private String vehicleType;               // e.g., BIKE, CAR, BICYCLE
    private String vehicleNumber;             // registration number
    private String licenseDocumentUrl;        // link to uploaded driving license (S3)
    private String idDocumentUrl;             // link to other ID (Aadhar/Passport etc.)
    private boolean verified = false;         // set by admin or auto verification flow

    private Boolean online = false;           // true when partner toggles "available"
    private Double currentLat;                // last known location
    private Double currentLng;
    private Instant lastSeen;                 // last heartbeat timestamp

    private Integer maxCapacity = 1;          // optional: how many orders they can carry
}