package com.example.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	Optional<CartItem> findByIdAndCartUserId(Long id, Long userId);

	Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
