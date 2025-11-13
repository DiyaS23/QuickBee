package com.quickbee.backend.controller;

import com.quickbee.backend.model.User;
import com.quickbee.backend.service.AssignmentService;
import com.quickbee.backend.service.PartnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final PartnerService partnerService;
    private final AssignmentService assignmentService;

    public PartnerController(PartnerService partnerService,AssignmentService assignmentService) {
        this.partnerService = partnerService;
        this.assignmentService=assignmentService;
    }

    @PostMapping("/apply")
    public ResponseEntity<User> applyAsDriver(@RequestParam String userId,
                                              @RequestParam String vehicleType,
                                              @RequestParam String vehicleNumber,
                                              @RequestParam String licenseUrl,
                                              @RequestParam String idDocUrl,
                                              @RequestParam(required = false) Integer maxCapacity) {
        User u = partnerService.applyAsDriver(userId, vehicleType, vehicleNumber, licenseUrl, idDocUrl, maxCapacity);
        return ResponseEntity.ok(u);
    }

    // Admin endpoint to verify/approve a partner
    @PostMapping("/{userId}/verify")
    public ResponseEntity<User> verifyPartner(@PathVariable String userId, @RequestParam boolean approve) {
        User u = partnerService.verifyPartnerByAdmin(userId, approve);
        return ResponseEntity.ok(u);
    }

    // Partner toggles availability
    // in PartnerController
    @PostMapping("/{userId}/availability")
    public ResponseEntity<User> setAvailability(@PathVariable String userId,
                                                @RequestParam boolean available,
                                                @RequestParam(required = false) Double lat,
                                                @RequestParam(required = false) Double lng) {
        User u = partnerService.setAvailability(userId, available, lat, lng);

        if (available) {
            // Try to assign orders up to capacity
            int capacity = u.getMaxCapacity() == null ? 1 : u.getMaxCapacity();
            for (int i = 0; i < capacity; i++) {
                boolean assigned = assignmentService.tryAssignToPartner(userId);
                if (!assigned) break;
            }
        }
        return ResponseEntity.ok(u);
    }

}
