package com.quickbee.backend.repository;

import com.quickbee.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String desc, Pageable pageable);
    List<Product> findTop10ByOrderByStockQuantityDesc();
    Page<Product> findByPriceBetweenAndCategoryIgnoreCase(Double min, Double max, String category, Pageable p);
    Page<Product> findByPriceBetween(Double min, Double max, Pageable p);

}