package com.example.ecommerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.WarehouseRequest;
import com.example.ecommerce.dto.WarehouseResponse;
import com.example.ecommerce.service.WarehouseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

	private final WarehouseService warehouseService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
	public ResponseEntity<List<WarehouseResponse>> getAll(
			@RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
		return ResponseEntity.ok(warehouseService.getAll(activeOnly));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
	public ResponseEntity<WarehouseResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(warehouseService.getById(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.create(request));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<WarehouseResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody WarehouseRequest request) {
		return ResponseEntity.ok(warehouseService.update(id, request));
	}
}
