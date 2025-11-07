package com.quickbee.backend.controller;

import com.quickbee.backend.dto.CreateOrderRequest;
import com.quickbee.backend.dto.OrderResponse;
import com.quickbee.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class OrderController {

    private final OrderService svc;

    public OrderController(OrderService svc) {
        this.svc = svc;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        OrderResponse resp = svc.createOrder(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/me")
    public Page<OrderResponse> myOrders(@RequestParam(defaultValue="0") int page,
                                        @RequestParam(defaultValue="10") int size) {
        return svc.listMyOrders(page, size);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable String id) {
        return svc.getOrderById(id);
    }
}
