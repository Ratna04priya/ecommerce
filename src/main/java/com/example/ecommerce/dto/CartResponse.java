package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

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
public class CartResponse {

	private Long cartId;
	private Long userId;
	private List<CartItemResponse> items;
	private int totalItems;
	private BigDecimal subtotal;
}
