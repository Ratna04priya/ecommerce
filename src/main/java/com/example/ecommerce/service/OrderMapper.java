package com.example.ecommerce.service;

import java.util.List;

import com.example.ecommerce.dto.OrderItemResponse;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;

public final class OrderMapper {

	private OrderMapper() {
	}

	public static OrderResponse toResponse(Order order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(item -> OrderItemResponse.builder()
						.id(item.getId())
						.productId(item.getProduct().getId())
						.productName(item.getProduct().getName())
						.productSku(item.getProduct().getSku())
						.warehouseId(item.getWarehouse().getId())
						.warehouseCode(item.getWarehouse().getCode())
						.quantity(item.getQuantity())
						.unitPrice(item.getUnitPrice())
						.lineTotal(item.getLineTotal())
						.build())
				.toList();

		return OrderResponse.builder()
				.id(order.getId())
				.orderNumber(order.getOrderNumber())
				.status(order.getStatus())
				.subtotal(order.getSubtotal())
				.taxAmount(order.getTaxAmount())
				.discountAmount(order.getDiscountAmount())
				.totalAmount(order.getTotalAmount())
				.discountCode(order.getDiscountCode())
				.shippingAddress(order.getShippingAddress())
				.items(items)
				.payment(toPaymentResponse(order.getPayment()))
				.createdAt(order.getCreatedAt())
				.build();
	}

	private static PaymentResponse toPaymentResponse(Payment payment) {
		if (payment == null) {
			return null;
		}
		return PaymentResponse.builder()
				.id(payment.getId())
				.amount(payment.getAmount())
				.status(payment.getStatus())
				.method(payment.getMethod())
				.transactionReference(payment.getTransactionReference())
				.paidAt(payment.getPaidAt())
				.build();
	}
}
