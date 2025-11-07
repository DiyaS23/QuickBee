package com.quickbee.backend.controller;

import com.quickbee.backend.dto.AddItemRequest;
import com.quickbee.backend.dto.CartResponse;
import com.quickbee.backend.dto.UpdateItemRequest;
import com.quickbee.backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasAnyRole('USER','ADMIN')") // all cart endpoints require auth
public class CartController {

    private final CartService svc;

    public CartController(CartService svc) {
        this.svc = svc;
    }

    @GetMapping
    public CartResponse getMyCart() {
        return svc.getMyCart();
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddItemRequest req) {
        return ResponseEntity.ok(svc.addItem(req));
    }

    @PutMapping("/item/{productId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable String productId,
                                                   @Valid @RequestBody UpdateItemRequest req) {
        return ResponseEntity.ok(svc.updateItem(productId, req));
    }

    @DeleteMapping("/item/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable String productId) {
        return ResponseEntity.ok(svc.removeItem(productId));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart() {
        return ResponseEntity.ok(svc.clearCart());
    }
}
