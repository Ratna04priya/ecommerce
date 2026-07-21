package com.example.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.OrderReturn;

public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {

	boolean existsByOrderId(Long orderId);

	Optional<OrderReturn> findByOrderId(Long orderId);
}
