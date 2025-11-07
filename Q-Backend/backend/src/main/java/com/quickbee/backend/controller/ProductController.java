package com.quickbee.backend.controller;

import com.quickbee.backend.dto.ProductRequest;
import com.quickbee.backend.model.Product;
import com.quickbee.backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService svc;

    @GetMapping
    public Page<Product> list(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size,
            @RequestParam(defaultValue="name") String sortBy,
            @RequestParam(defaultValue="ASC") String sortDir
    ) {
        return svc.getAllProducts(page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable String id) {
        return svc.getProductById(id);
    }

    @GetMapping("/category/{category}")
    public Page<Product> byCategory(@PathVariable String category,
                                    @RequestParam(defaultValue="0") int page,
                                    @RequestParam(defaultValue="10") int size) {
        return svc.getProductsByCategory(category, page, size);
    }

    @GetMapping("/search")
    public Page<Product> search(@RequestParam String q,
                                @RequestParam(defaultValue="0") int page,
                                @RequestParam(defaultValue="10") int size) {
        return svc.search(q, page, size);
    }

    // ADMIN endpoints (protect later)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest req) {
        Product p = svc.createProduct(req);
        return ResponseEntity.ok(p);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product update(@PathVariable String id, @Valid @RequestBody ProductRequest req) {
        return svc.updateProduct(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        svc.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/filter")
    public Page<Product> filter(@RequestParam(required=false) Double minPrice,
                                @RequestParam(required=false) Double maxPrice,
                                @RequestParam(required=false) String category,
                                @RequestParam(defaultValue="0") int page,
                                @RequestParam(defaultValue="10") int size) {
        return svc.filter(minPrice, maxPrice, category, page, size);
    }

}
