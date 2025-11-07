package com.quickbee.backend.dto;

import jakarta.validation.constraints.Min;

public class UpdateItemRequest {
    @Min(0) // 0 removes item
    private int qty;

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}
