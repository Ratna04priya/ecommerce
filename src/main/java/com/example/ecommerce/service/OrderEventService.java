package com.example.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.ecommerce.entity.AuditLog;
import com.example.ecommerce.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

/**
 * Downstream pipeline that must not block the checkout HTTP response.
 */
@Service
@RequiredArgsConstructor
public class OrderEventService {

	private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);

	private final AuditLogRepository auditLogRepository;

	@Async
	public void onOrderPlaced(String orderNumber, Long customerId, String totalAmount) {
		String message = "Order " + orderNumber + " placed by customer " + customerId
				+ " for amount " + totalAmount;
		log.info("[NOTIFICATION] Customer {}: Your order {} is confirmed. Total: {}",
				customerId, orderNumber, totalAmount);
		log.info("[FULFILLMENT] Route order {} to warehouses based on reserved inventory", orderNumber);

		auditLogRepository.save(AuditLog.builder()
				.eventType("ORDER_PLACED")
				.message(message)
				.referenceId(orderNumber)
				.build());
	}
}
