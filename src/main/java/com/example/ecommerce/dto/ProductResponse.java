package com.example.ecommerce.dto;

import java.math.BigDecimal;
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
public class ProductResponse {

	private Long id;
	private String name;
	private String description;
	private String sku;
	private String category;
	private BigDecimal price;
	private boolean active;
	private Instant createdAt;
	private Instant updatedAt;
}
