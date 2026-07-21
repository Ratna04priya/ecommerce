package com.example.ecommerce.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

	private Long id;
	private Long productId;
	private String productName;
	private String productSku;
	private Long warehouseId;
	private String warehouseCode;
	private String warehouseName;
	private int quantityAvailable;
	private int quantityReserved;
	private Instant updatedAt;
}
