package com.example.ecommerce.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

	@NotBlank
	@Size(max = 200)
	private String name;

	@Size(max = 2000)
	private String description;

	@NotBlank
	@Size(max = 80)
	private String sku;

	@NotBlank
	@Size(max = 100)
	private String category;

	@NotNull
	@DecimalMin(value = "0.01", message = "Price must be greater than 0")
	private BigDecimal price;

	private Boolean active;
}
