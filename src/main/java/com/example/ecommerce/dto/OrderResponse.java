package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.ecommerce.entity.OrderStatus;

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
public class OrderResponse {

	private Long id;
	private String orderNumber;
	private OrderStatus status;
	private BigDecimal subtotal;
	private BigDecimal taxAmount;
	private BigDecimal discountAmount;
	private BigDecimal totalAmount;
	private String discountCode;
	private String shippingAddress;
	private List<OrderItemResponse> items;
	private PaymentResponse payment;
	private Instant createdAt;
}
