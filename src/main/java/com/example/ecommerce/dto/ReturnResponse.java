package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.ecommerce.entity.ReturnStatus;

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
public class ReturnResponse {

	private Long id;
	private Long orderId;
	private String orderNumber;
	private String reason;
	private ReturnStatus status;
	private BigDecimal refundAmount;
	private String refundReference;
	private Instant createdAt;
}
