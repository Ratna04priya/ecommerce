package com.example.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

	private final CartService cartService;

	@GetMapping
	public ResponseEntity<CartResponse> getCart() {
		return ResponseEntity.ok(cartService.getCart(SecurityUtils.currentUser().getId()));
	}

	@PostMapping("/add")
	public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddToCartRequest request) {
		return ResponseEntity.ok(cartService.addItem(SecurityUtils.currentUser().getId(), request));
	}

	@DeleteMapping("/remove/{itemId}")
	public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
		return ResponseEntity.ok(cartService.removeItem(SecurityUtils.currentUser().getId(), itemId));
	}
}
