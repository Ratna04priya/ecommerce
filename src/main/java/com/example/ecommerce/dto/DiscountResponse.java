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
public class DiscountResponse {

	private Long id;
	private String code;
	private BigDecimal percentOff;
	private boolean active;
	private Instant expiresAt;
	private Instant createdAt;
}
