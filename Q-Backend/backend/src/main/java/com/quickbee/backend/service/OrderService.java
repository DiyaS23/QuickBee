package com.quickbee.backend.service;

import com.quickbee.backend.dto.CreateOrderRequest;
import com.quickbee.backend.dto.OrderItemResponse;
import com.quickbee.backend.dto.OrderResponse;
import com.quickbee.backend.exception.ResourceNotFoundException;
import com.quickbee.backend.model.*;
import com.quickbee.backend.model.enums.OrderStatus;
import com.quickbee.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final AddressRepository addressRepo;
    private final UserRepository userRepo;
    private final MongoTemplate mongoTemplate;

    private static final double DELIVERY_FEE_FLAT = 19.0; // adjust as needed

    public OrderService(OrderRepository orderRepo,
                        CartRepository cartRepo,
                        ProductRepository productRepo,
                        AddressRepository addressRepo,
                        UserRepository userRepo,
                        MongoTemplate mongoTemplate) {
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.addressRepo = addressRepo;
        this.userRepo = userRepo;
        this.mongoTemplate = mongoTemplate;
    }

    /* ================= Create order ================= */

    @Transactional // note: Mongo multi-doc transactions require replica set; else we do manual rollback
    public OrderResponse createOrder(CreateOrderRequest req) {
        String userId = getCurrentUserId();

        // 1) Load cart
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // 2) Verify address ownership
        Address address = addressRepo.findByIdAndUserId(req.getAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // 3) Re-check stock & prepare shortages
        List<Shortage> shortages = new ArrayList<>();
        Map<String, Product> productMap = new HashMap<>();

        for (CartItem ci : cart.getItems()) {
            Product p = productRepo.findById(ci.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + ci.getProductId()));
            productMap.put(p.getId(), p);
            int stock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
            if (ci.getQty() > stock) {
                shortages.add(new Shortage(p.getId(), ci.getQty(), stock));
            }
        }
        if (!shortages.isEmpty()) {
            throw new InsufficientStockException(shortages);
        }

        // 4) Deduct stock atomically per item; keep track for rollback if needed
        List<Deducted> deducted = new ArrayList<>();
        try {
            for (CartItem ci : cart.getItems()) {
                String pid = ci.getProductId();
                int qty = ci.getQty();

                Query q = new Query(Criteria.where("_id").is(pid)
                        .and("stockQuantity").gte(qty));
                Update u = new Update().inc("stockQuantity", -qty);

                FindAndModifyOptions opts = FindAndModifyOptions.options().returnNew(true);
                Product updated = mongoTemplate.findAndModify(q, u, opts, Product.class);

                if (updated == null) {
                    // could not deduct for this item -> rollback previous and fail
                    rollbackDeductions(deducted);
                    throw new InsufficientStockException(
                            List.of(new Shortage(pid, qty, productMap.get(pid).getStockQuantity()))
                    );
                } else {
                    deducted.add(new Deducted(pid, qty));
                }
            }

            // 5) Build order snapshot
            List<OrderItem> orderItems = cart.getItems().stream().map(ci -> {
                Product p = productMap.get(ci.getProductId());
                return new OrderItem(
                        p.getId(),
                        p.getName(),
                        p.getImageUrl(),
                        p.getPrice(),   // price at purchase time
                        ci.getQty()
                );
            }).collect(Collectors.toList());

            double subtotal = orderItems.stream()
                    .mapToDouble(i -> i.getPriceAtPurchase() * i.getQty())
                    .sum();
            subtotal = round2(subtotal);

            double deliveryFee = DELIVERY_FEE_FLAT;
            double total = round2(subtotal + deliveryFee);

            AddressSnapshot snap = new AddressSnapshot(
                    safe(address.getName()),
                    safe(address.getPhone()),
                    safe(getField(address, "line1")),
                    safe(getField(address, "line2")),
                    safe(getField(address, "city")),
                    safe(getField(address, "state")),
                    safe(getField(address, "pincode"))
            );

            Order order = new Order();
            order.setUserId(userId);
            order.setAddressId(address.getId());
            order.setAddress(snap);
            order.setItems(orderItems);
            order.setSubtotal(subtotal);
            order.setDeliveryFee(deliveryFee);
            order.setTotal(total);
            order.setStatus(OrderStatus.PENDING);

            Order saved = orderRepo.save(order);

            // 6) Clear cart
            cart.getItems().clear();
            cart.setSubtotal(0.0);
            cart.setTotalItems(0);
            cartRepo.save(cart);

            return toResponse(saved);

        } catch (RuntimeException ex) {
            // Safety: rollback stock if we failed after some deductions
            rollbackDeductions(deducted);
            throw ex;
        }
    }

    /* ================= Queries ================= */

    public Page<OrderResponse> listMyOrders(int page, int size) {
        String userId = getCurrentUserId();
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    public OrderResponse getOrderById(String id) {
        String userId = getCurrentUserId();
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean isOwner = order.getUserId().equals(userId);
        boolean isAdmin = isCurrentUserAdmin();

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Forbidden");
        }
        return toResponse(order);
    }

    /* ================= Helpers ================= */

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("Unauthenticated");
        String email = (auth.getPrincipal() instanceof UserDetails ud) ? ud.getUsername() : String.valueOf(auth.getPrincipal());
        User u = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return u.getId();
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private void rollbackDeductions(List<Deducted> deducted) {
        for (Deducted d : deducted) {
            Query q = new Query(Criteria.where("_id").is(d.productId));
            Update u = new Update().inc("stockQuantity", d.qty);
            mongoTemplate.findAndModify(q, u, FindAndModifyOptions.options().returnNew(true), Product.class);
        }
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProductId(),
                        i.getName(),
                        i.getImageUrl(),
                        i.getPriceAtPurchase(),
                        i.getQty(),
                        round2(i.getPriceAtPurchase() * i.getQty())
                ))
                .collect(Collectors.toList());

        OrderResponse.AddressDto addr = new OrderResponse.AddressDto(
                safe(o.getAddress().getName()),
                safe(o.getAddress().getPhone()),
                safe(o.getAddress().getLine1()),
                safe(o.getAddress().getLine2()),
                safe(o.getAddress().getCity()),
                safe(o.getAddress().getState()),
                safe(o.getAddress().getPincode())
        );

        return new OrderResponse(
                o.getId(),
                o.getStatus(),
                o.getSubtotal(),
                o.getDeliveryFee(),
                o.getTotal(),
                items,
                addr,
                o.getCreatedAt()
        );
    }

    private String safe(String s) { return s == null ? "" : s; }

    // Accessor helper to avoid compile issues if your Address field differs slightly
    private String getField(Address address, String field) {
        try {
            var f = Address.class.getDeclaredField(field);
            f.setAccessible(true);
            Object v = f.get(address);
            return v == null ? "" : v.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    public record Shortage(String productId, int requested, Integer available) {}
    private record Deducted(String productId, int qty) {}

    public static class InsufficientStockException extends RuntimeException {
        private final List<Shortage> items;
        public InsufficientStockException(List<Shortage> items) {
            super("Insufficient stock");
            this.items = items;
        }
        public List<Shortage> getItems() { return items; }
    }
}
