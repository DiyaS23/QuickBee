package com.quickbee.backend.controller;

import com.quickbee.backend.dto.UpdateStatusRequest;
import com.quickbee.backend.model.Order;
import com.quickbee.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Secures ALL methods in this controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    // GET /api/admin/orders
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    // PUT /api/admin/orders/{id}/status
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id,
                                               @RequestBody UpdateStatusRequest request) {
        try {
            Order order = adminService.updateOrderStatus(id, request.getStatus());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}