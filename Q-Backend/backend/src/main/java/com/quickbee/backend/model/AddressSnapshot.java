package com.quickbee.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class AddressSnapshot {
    // Adjust these fields to match your Address model; keep snapshot minimal but useful
    private String name;    // label or recipient name
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String pincode;
}
