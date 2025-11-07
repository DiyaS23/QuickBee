package com.quickbee.backend.service;

import com.quickbee.backend.dto.AddItemRequest;
import com.quickbee.backend.dto.CartItemResponse;
import com.quickbee.backend.dto.CartResponse;
import com.quickbee.backend.dto.UpdateItemRequest;
import com.quickbee.backend.exception.ResourceNotFoundException;
import com.quickbee.backend.model.Cart;
import com.quickbee.backend.model.CartItem;
import com.quickbee.backend.model.Product;
import com.quickbee.backend.model.User;
import com.quickbee.backend.repository.CartRepository;
import com.quickbee.backend.repository.ProductRepository;
import com.quickbee.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public CartService(CartRepository cartRepo,
                       ProductRepository productRepo,
                       UserRepository userRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    /* ========= Public API ========= */

    public CartResponse getMyCart() {
        Cart cart = getOrCreateCart(getCurrentUserId());
        return toResponse(cart, /*refreshPrices*/ true);
    }

    @Transactional
    public CartResponse addItem(AddItemRequest req) {
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);

        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.getProductId()));

        int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int desired = req.getQty();
        int finalQty = Math.max(1, Math.min(desired, stock)); // clamp to [1..stock]

        // if stock is 0, reject
        if (finalQty == 0) {
            throw new ResourceNotFoundException("Product out of stock: " + product.getId());
        }

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existing == null) {
            cart.getItems().add(new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    product.getPrice(),
                    finalQty
            ));
        } else {
            int newQty = Math.min(existing.getQty() + finalQty, stock);
            existing.setQty(newQty);
            // update snapshot price/name in case they changed
            existing.setPrice(product.getPrice());
            existing.setName(product.getName());
            existing.setImageUrl(product.getImageUrl());
        }

        recompute(cart, /*refreshPrices*/ false);
        cartRepo.save(cart);
        return toResponse(cart, false);
    }

    @Transactional
    public CartResponse updateItem(String productId, UpdateItemRequest req) {
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not in cart: " + productId));

        int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int qty = Math.max(0, Math.min(req.getQty(), stock)); // clamp to [0..stock]

        if (qty == 0) {
            cart.getItems().remove(existing);
        } else {
            existing.setQty(qty);
            // refresh snapshots
            existing.setPrice(product.getPrice());
            existing.setName(product.getName());
            existing.setImageUrl(product.getImageUrl());
        }

        recompute(cart, /*refreshPrices*/ false);
        cartRepo.save(cart);
        return toResponse(cart, false);
    }

    @Transactional
    public CartResponse removeItem(String productId) {
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);

        boolean removed = cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        if (!removed) {
            throw new ResourceNotFoundException("Item not in cart: " + productId);
        }

        recompute(cart, false);
        cartRepo.save(cart);
        return toResponse(cart, false);
    }

    @Transactional
    public CartResponse clearCart() {
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        recompute(cart, false);
        cartRepo.save(cart);
        return toResponse(cart, false);
    }

    /* ========= Helpers ========= */

    private Cart getOrCreateCart(String userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            c.setItems(new ArrayList<>());
            c.setSubtotal(0.0);
            c.setTotalItems(0);
            return cartRepo.save(c);
        });
    }

    private String getCurrentUserId() {
        // JWT contains email as username; resolve the DB user to get the userId
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("Unauthenticated");
        Object principal = auth.getPrincipal();
        String email = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return user.getId();
    }

    private void recompute(Cart cart, boolean refreshPrices) {
        double subtotal = 0.0;
        int totalItems = 0;

        if (refreshPrices) {
            // refresh each item's price/name/image from current product state
            for (CartItem item : cart.getItems()) {
                Product product = productRepo.findById(item.getProductId())
                        .orElse(null);
                if (product != null) {
                    item.setPrice(product.getPrice());
                    item.setName(product.getName());
                    item.setImageUrl(product.getImageUrl());
                    // also re-clamp qty to current stock in case it dropped
                    int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
                    if (item.getQty() > stock) item.setQty(Math.max(0, stock));
                }
            }
            // remove any that became zero after clamp
            cart.getItems().removeIf(i -> i.getQty() <= 0);
        }

        for (CartItem item : cart.getItems()) {
            subtotal += (item.getPrice() * item.getQty());
            totalItems += item.getQty();
        }

        cart.setSubtotal(round2(subtotal));
        cart.setTotalItems(totalItems);

        // (optional) keep a consistent order for UX
        cart.getItems().sort(Comparator.comparing(CartItem::getName, String.CASE_INSENSITIVE_ORDER));
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private CartResponse toResponse(Cart cart, boolean refreshPrices) {
        if (refreshPrices) recompute(cart, true);
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getProductId(),
                        i.getName(),
                        i.getImageUrl(),
                        i.getPrice(),
                        i.getQty(),
                        round2(i.getPrice() * i.getQty())
                ))
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items,
                cart.getSubtotal(),
                cart.getTotalItems()
        );
    }
}
