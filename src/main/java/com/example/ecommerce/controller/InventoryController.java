package com.example.ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.InventoryResponse;
import com.example.ecommerce.dto.InventoryUpsertRequest;
import com.example.ecommerce.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

	private final InventoryService inventoryService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
	public ResponseEntity<List<InventoryResponse>> getAll(
			@RequestParam(required = false) Long productId,
			@RequestParam(required = false) Long warehouseId) {
		return ResponseEntity.ok(inventoryService.getAll(productId, warehouseId));
	}

	@PutMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<InventoryResponse> upsert(@Valid @RequestBody InventoryUpsertRequest request) {
		return ResponseEntity.ok(inventoryService.upsert(request));
	}
}
