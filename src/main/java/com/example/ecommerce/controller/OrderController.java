package com.example.ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.dto.UpdateOrderStatusRequest;
import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'WAREHOUSE_STAFF')")
	public ResponseEntity<List<OrderResponse>> listOrders() {
		return ResponseEntity.ok(orderService.listOrders(SecurityUtils.currentUser()));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'WAREHOUSE_STAFF')")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.getOrder(id, SecurityUtils.currentUser()));
	}

	@PutMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'WAREHOUSE_STAFF')")
	public ResponseEntity<OrderResponse> updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody UpdateOrderStatusRequest request) {
		return ResponseEntity.ok(
				orderService.updateStatus(id, request.getStatus(), SecurityUtils.currentUser()));
	}
}
