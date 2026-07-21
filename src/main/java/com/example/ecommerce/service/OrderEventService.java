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

	@Async
	public void onOrderStatusChanged(String orderNumber, String fromStatus, String toStatus) {
		String message = "Order " + orderNumber + " status changed from " + fromStatus + " to " + toStatus;
		log.info("[NOTIFICATION] Order {} is now {}", orderNumber, toStatus);
		auditLogRepository.save(AuditLog.builder()
				.eventType("ORDER_STATUS_CHANGED")
				.message(message)
				.referenceId(orderNumber)
				.build());
	}

	@Async
	public void onOrderReturned(String orderNumber, String refundReference, String refundAmount) {
		String message = "Order " + orderNumber + " returned. Refund " + refundReference
				+ " amount " + refundAmount;
		log.info("[NOTIFICATION] Refund {} issued for order {} amount {}",
				refundReference, orderNumber, refundAmount);
		auditLogRepository.save(AuditLog.builder()
				.eventType("ORDER_RETURNED")
				.message(message)
				.referenceId(orderNumber)
				.build());
	}
}
