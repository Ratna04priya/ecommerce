package com.example.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryUpsertRequest {

	@NotNull
	private Long productId;

	@NotNull
	private Long warehouseId;

	@NotNull
	@Min(value = 0, message = "quantityAvailable cannot be negative")
	private Integer quantityAvailable;
}
