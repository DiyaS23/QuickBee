package com.quickbee.backend.model.enums;

public enum OrderStatus {
    PENDING,          // placed, awaiting payment confirmation
    CONFIRMED,        // paid/confirmed
    ASSIGNED,         // assigned to a partner (waiting acceptance)
    ACCEPTED,         // partner accepted (but not yet picked)
    OUT_FOR_DELIVERY, // rider picked up
    DELIVERED,        // completed
    CANCELLED         // cancelled (manual or failed)
}
