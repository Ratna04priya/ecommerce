package com.example.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.Discount;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

	Optional<Discount> findByCodeIgnoreCase(String code);
}
