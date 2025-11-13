package com.quickbee.backend.model.enums;

public enum PartnerStatus {
    PENDING_VERIFICATION, // applied but not verified/approved
    VERIFIED,             // verified by admin / auto-verified
    ACTIVE,               // approved but currently offline
    AVAILABLE,            // online and ready to accept assignments
    UNAVAILABLE,
    ON_DELIVERY,  // temporarily not taking assignments (break)
    SUSPENDED             // banned/suspended
}

