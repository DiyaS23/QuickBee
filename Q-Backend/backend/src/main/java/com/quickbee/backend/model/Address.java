package com.quickbee.backend.model;

import jakarta.validation.constraints.NotBlank; // <-- Note: jakarta.validation
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant; // Use Instant for modern date handling

@Data
@NoArgsConstructor
@Document(collection = "addresses")
public class Address {

    @Id
    private String id;

    @Indexed // Good idea to index this
    private String userId; // Reference to User.id

    @NotBlank(message = "Address name is required (e.g., 'Home', 'Office')")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Address line 1 is required")
    private String line1;

    private String line2; // Optional

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    // We'll leave these for a future "geolocation" feature
    private Double lat;
    private Double lng;

    @CreatedDate
    private Instant createdAt; // Automatically set

    @LastModifiedDate
    private Instant updatedAt; // Automatically set
}