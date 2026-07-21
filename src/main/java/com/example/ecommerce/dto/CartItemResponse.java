package com.example.ecommerce.dto;

import java.math.BigDecimal;

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
public class CartItemResponse {

	private Long id;
	private Long productId;
	private String productName;
	private String productSku;
	private BigDecimal unitPrice;
	private int quantity;
	private BigDecimal lineTotal;
}
