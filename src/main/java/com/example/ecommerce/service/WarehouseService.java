package com.example.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.WarehouseRequest;
import com.example.ecommerce.dto.WarehouseResponse;
import com.example.ecommerce.entity.Warehouse;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarehouseService {

	private final WarehouseRepository warehouseRepository;

	@Transactional(readOnly = true)
	public List<WarehouseResponse> getAll(Boolean activeOnly) {
		List<Warehouse> warehouses = Boolean.TRUE.equals(activeOnly)
				? warehouseRepository.findByActiveTrue()
				: warehouseRepository.findAll();
		return warehouses.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public WarehouseResponse getById(Long id) {
		return toResponse(findWarehouse(id));
	}

	@Transactional
	public WarehouseResponse create(WarehouseRequest request) {
		String code = request.getCode().trim().toUpperCase();
		if (warehouseRepository.existsByCode(code)) {
			throw new ApiException("Warehouse code already exists: " + code, HttpStatus.CONFLICT);
		}
		if (warehouseRepository.existsByName(request.getName().trim())) {
			throw new ApiException("Warehouse name already exists", HttpStatus.CONFLICT);
		}

		Warehouse warehouse = Warehouse.builder()
				.name(request.getName().trim())
				.code(code)
				.location(request.getLocation().trim())
				.active(request.getActive() == null || request.getActive())
				.build();

		return toResponse(warehouseRepository.save(warehouse));
	}

	@Transactional
	public WarehouseResponse update(Long id, WarehouseRequest request) {
		Warehouse warehouse = findWarehouse(id);
		String code = request.getCode().trim().toUpperCase();

		warehouseRepository.findByCode(code)
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new ApiException("Warehouse code already exists: " + code, HttpStatus.CONFLICT);
				});

		if (!warehouse.getName().equalsIgnoreCase(request.getName().trim())
				&& warehouseRepository.existsByName(request.getName().trim())) {
			throw new ApiException("Warehouse name already exists", HttpStatus.CONFLICT);
		}

		warehouse.setName(request.getName().trim());
		warehouse.setCode(code);
		warehouse.setLocation(request.getLocation().trim());
		if (request.getActive() != null) {
			warehouse.setActive(request.getActive());
		}

		return toResponse(warehouseRepository.save(warehouse));
	}

	public Warehouse findWarehouse(Long id) {
		return warehouseRepository.findById(id)
				.orElseThrow(() -> new ApiException("Warehouse not found: " + id, HttpStatus.NOT_FOUND));
	}

	private WarehouseResponse toResponse(Warehouse warehouse) {
		return WarehouseResponse.builder()
				.id(warehouse.getId())
				.name(warehouse.getName())
				.code(warehouse.getCode())
				.location(warehouse.getLocation())
				.active(warehouse.isActive())
				.createdAt(warehouse.getCreatedAt())
				.build();
	}
}
