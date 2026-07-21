package com.example.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.InventoryResponse;
import com.example.ecommerce.dto.InventoryUpsertRequest;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Warehouse;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

	private final InventoryRepository inventoryRepository;
	private final ProductRepository productRepository;
	private final WarehouseService warehouseService;

	@Transactional(readOnly = true)
	public List<InventoryResponse> getAll(Long productId, Long warehouseId) {
		List<Inventory> rows;
		if (productId != null && warehouseId != null) {
			rows = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
					.map(List::of)
					.orElse(List.of());
		} else if (productId != null) {
			rows = inventoryRepository.findByProductId(productId);
		} else if (warehouseId != null) {
			rows = inventoryRepository.findByWarehouseId(warehouseId);
		} else {
			rows = inventoryRepository.findAll();
		}
		return rows.stream().map(this::toResponse).toList();
	}

	@Transactional
	public InventoryResponse upsert(InventoryUpsertRequest request) {
		Product product = productRepository.findById(request.getProductId())
				.orElseThrow(() -> new ApiException(
						"Product not found: " + request.getProductId(), HttpStatus.NOT_FOUND));

		if (!product.isActive()) {
			throw new ApiException("Cannot stock inactive product: " + product.getSku(), HttpStatus.BAD_REQUEST);
		}

		Warehouse warehouse = warehouseService.findWarehouse(request.getWarehouseId());
		if (!warehouse.isActive()) {
			throw new ApiException("Cannot stock inactive warehouse: " + warehouse.getCode(), HttpStatus.BAD_REQUEST);
		}

		Inventory inventory = inventoryRepository
				.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
				.orElseGet(() -> Inventory.builder()
						.product(product)
						.warehouse(warehouse)
						.quantityAvailable(0)
						.quantityReserved(0)
						.build());

		int newAvailable = request.getQuantityAvailable();
		if (newAvailable < inventory.getQuantityReserved()) {
			throw new ApiException(
					"quantityAvailable cannot be less than quantityReserved ("
							+ inventory.getQuantityReserved() + ")",
					HttpStatus.BAD_REQUEST);
		}

		inventory.setQuantityAvailable(newAvailable);
		return toResponse(inventoryRepository.save(inventory));
	}

	private InventoryResponse toResponse(Inventory inventory) {
		return InventoryResponse.builder()
				.id(inventory.getId())
				.productId(inventory.getProduct().getId())
				.productName(inventory.getProduct().getName())
				.productSku(inventory.getProduct().getSku())
				.warehouseId(inventory.getWarehouse().getId())
				.warehouseCode(inventory.getWarehouse().getCode())
				.warehouseName(inventory.getWarehouse().getName())
				.quantityAvailable(inventory.getQuantityAvailable())
				.quantityReserved(inventory.getQuantityReserved())
				.updatedAt(inventory.getUpdatedAt())
				.build();
	}
}
