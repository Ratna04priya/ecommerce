package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.ecommerce.entity.PaymentStatus;

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
public class PaymentResponse {

	private Long id;
	private BigDecimal amount;
	private PaymentStatus status;
	private String method;
	private String transactionReference;
	private Instant paidAt;
}
