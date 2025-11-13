package com.quickbee.backend.controller;

import com.quickbee.backend.service.AssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/{assignmentId}/accept")
    public ResponseEntity<?> accept(@PathVariable String assignmentId, @RequestParam String partnerId) {
        boolean ok = assignmentService.partnerAccepts(assignmentId, partnerId);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PostMapping("/{assignmentId}/reject")
    public ResponseEntity<?> reject(@PathVariable String assignmentId, @RequestParam String partnerId) {
        boolean ok = assignmentService.partnerRejects(assignmentId, partnerId);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PostMapping("/{assignmentId}/picked")
    public ResponseEntity<?> picked(@PathVariable String assignmentId, @RequestParam String partnerId) {
        boolean ok = assignmentService.partnerPicked(assignmentId, partnerId);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PostMapping("/{assignmentId}/delivered")
    public ResponseEntity<?> delivered(@PathVariable String assignmentId, @RequestParam String partnerId) {
        boolean ok = assignmentService.partnerDelivered(assignmentId, partnerId);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
