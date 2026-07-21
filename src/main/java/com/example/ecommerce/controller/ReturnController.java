package com.example.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.CreateReturnRequest;
import com.example.ecommerce.dto.ReturnResponse;
import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.service.ReturnService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

	private final ReturnService returnService;

	@PostMapping
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ReturnResponse> createReturn(@Valid @RequestBody CreateReturnRequest request) {
		ReturnResponse response = returnService.createReturn(request, SecurityUtils.currentUser());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/order/{orderId}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'WAREHOUSE_STAFF')")
	public ResponseEntity<ReturnResponse> getByOrder(@PathVariable Long orderId) {
		return ResponseEntity.ok(returnService.getByOrderId(orderId, SecurityUtils.currentUser()));
	}
}
