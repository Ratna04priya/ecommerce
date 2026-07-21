package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscountRequest {

	@NotBlank
	@Size(max = 40)
	private String code;

	@NotNull
	@DecimalMin("0.01")
	@DecimalMax("100.00")
	private BigDecimal percentOff;

	private Boolean active;

	private Instant expiresAt;
}
