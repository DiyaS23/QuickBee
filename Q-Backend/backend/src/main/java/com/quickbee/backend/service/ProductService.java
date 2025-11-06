package com.quickbee.backend.service;

import com.quickbee.backend.dto.ProductRequest;
import com.quickbee.backend.exception.ResourceNotFoundException;
import com.quickbee.backend.model.Product;
import com.quickbee.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    public Product createProduct(ProductRequest req) {
        Product p = new Product();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        p.setCategory(req.getCategory());
        p.setImageUrl(req.getImageUrl());
        return repo.save(p);
    }

    public Page<Product> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort.Direction dir = Sort.Direction.fromOptionalString(sortDir).orElse(Sort.Direction.ASC);
        Sort sort = Sort.by(dir, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return repo.findAll(pageable);
    }

    public Product getProductById(String id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public Page<Product> getProductsByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findByCategory(category, pageable);
    }

    public Page<Product> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String regex = ".*" + q + ".*";
        return repo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q, pageable);
    }

    public Product updateProduct(String id, ProductRequest req) {
        Product p = getProductById(id);
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        p.setCategory(req.getCategory());
        p.setImageUrl(req.getImageUrl());
        return repo.save(p);
    }

    public void deleteProduct(String id) {
        repo.deleteById(id);
    }

    // additional helpers
    public List<Product> topStocked() {
        return repo.findTop10ByOrderByStockQuantityDesc();
    }
    public Page<Product> filter(Double min, Double max, String category, int page, int size) {
        double lo = min != null ? min : 0.0;
        double hi = max != null ? max : Double.MAX_VALUE;
        Pageable p = PageRequest.of(page, size);
        return (category != null && !category.isBlank())
                ? repo.findByPriceBetweenAndCategoryIgnoreCase(lo, hi, category, p)
                : repo.findByPriceBetween(lo, hi, p);
    }

}