package com.example.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findBySku(String sku);

	boolean existsBySku(String sku);

	List<Product> findByActiveTrue();

	List<Product> findByCategoryIgnoreCase(String category);

	List<Product> findByActiveTrueAndCategoryIgnoreCase(String category);
}
