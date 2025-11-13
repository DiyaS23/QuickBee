package com.quickbee.backend.model.enums;

public enum AssignmentStatus {
    CREATED,   // assignment created and waiting partner accept
    ACCEPTED,
    PICKED,
    REJECTED,
    TIMEOUT,   // acceptance timeout
    PICKUP_TIMEOUT, // accepted but not picked within allowed window
    COMPLETED,
    FAILED
}
