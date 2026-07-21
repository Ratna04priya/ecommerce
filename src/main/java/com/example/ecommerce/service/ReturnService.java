package com.example.ecommerce.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.CreateReturnRequest;
import com.example.ecommerce.dto.ReturnResponse;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderReturn;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.PaymentStatus;
import com.example.ecommerce.entity.ReturnStatus;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.OrderReturnRepository;
import com.example.ecommerce.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReturnService {

	private final OrderRepository orderRepository;
	private final OrderReturnRepository orderReturnRepository;
	private final InventoryRepository inventoryRepository;
	private final OrderEventService orderEventService;

	@Transactional
	public ReturnResponse createReturn(CreateReturnRequest request, UserPrincipal principal) {
		Order order = orderRepository.findByIdWithDetails(request.getOrderId())
				.orElseThrow(() -> new ApiException(
						"Order not found: " + request.getOrderId(), HttpStatus.NOT_FOUND));

		if (!order.getCustomer().getId().equals(principal.getId())) {
			throw new ApiException("You can only return your own orders", HttpStatus.FORBIDDEN);
		}

		if (order.getStatus() != OrderStatus.DELIVERED) {
			throw new ApiException(
					"Only DELIVERED orders can be returned. Current status: " + order.getStatus(),
					HttpStatus.BAD_REQUEST);
		}

		if (orderReturnRepository.existsByOrderId(order.getId())) {
			throw new ApiException("A return already exists for this order", HttpStatus.CONFLICT);
		}

		Payment payment = order.getPayment();
		if (payment == null || payment.getStatus() != PaymentStatus.SUCCESS) {
			throw new ApiException("Order payment is not eligible for refund", HttpStatus.BAD_REQUEST);
		}

		restockInventory(order);

		String refundReference = "RFD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
		payment.setStatus(PaymentStatus.REFUNDED);

		OrderReturn orderReturn = OrderReturn.builder()
				.order(order)
				.reason(request.getReason().trim())
				.status(ReturnStatus.REFUNDED)
				.refundAmount(order.getTotalAmount())
				.refundReference(refundReference)
				.build();

		order.setStatus(OrderStatus.RETURNED);
		orderRepository.save(order);
		OrderReturn saved = orderReturnRepository.save(orderReturn);

		orderEventService.onOrderReturned(order.getOrderNumber(), refundReference, order.getTotalAmount().toPlainString());

		return toResponse(saved, order);
	}

	@Transactional(readOnly = true)
	public ReturnResponse getByOrderId(Long orderId, UserPrincipal principal) {
		OrderReturn orderReturn = orderReturnRepository.findByOrderId(orderId)
				.orElseThrow(() -> new ApiException("Return not found for order: " + orderId, HttpStatus.NOT_FOUND));

		Order order = orderReturn.getOrder();
		boolean isOwner = order.getCustomer().getId().equals(principal.getId());
		boolean isStaff = principal.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
						|| a.getAuthority().equals("ROLE_WAREHOUSE_STAFF"));

		if (!isOwner && !isStaff) {
			throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
		}

		return toResponse(orderReturn, order);
	}

	private void restockInventory(Order order) {
		for (OrderItem item : order.getItems()) {
			Inventory inventory = inventoryRepository
					.findForUpdate(item.getProduct().getId(), item.getWarehouse().getId())
					.orElseThrow(() -> new ApiException(
							"Inventory missing for product " + item.getProduct().getSku(),
							HttpStatus.CONFLICT));

			inventory.setQuantityAvailable(inventory.getQuantityAvailable() + item.getQuantity());
			inventoryRepository.save(inventory);
		}
	}

	private ReturnResponse toResponse(OrderReturn orderReturn, Order order) {
		return ReturnResponse.builder()
				.id(orderReturn.getId())
				.orderId(order.getId())
				.orderNumber(order.getOrderNumber())
				.reason(orderReturn.getReason())
				.status(orderReturn.getStatus())
				.refundAmount(orderReturn.getRefundAmount())
				.refundReference(orderReturn.getRefundReference())
				.createdAt(orderReturn.getCreatedAt())
				.build();
	}
}
