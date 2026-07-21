package com.example.ecommerce.service;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
			OrderStatus.PLACED, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
			OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PACKED, OrderStatus.CANCELLED),
			OrderStatus.PACKED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
			OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED),
			OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class),
			OrderStatus.RETURNED, EnumSet.noneOf(OrderStatus.class),
			OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class)
	);

	private final OrderRepository orderRepository;
	private final InventoryRepository inventoryRepository;
	private final OrderEventService orderEventService;

	@Transactional(readOnly = true)
	public List<OrderResponse> listOrders(UserPrincipal principal) {
		List<Order> orders;
		if (hasRole(principal, "ROLE_ADMIN") || hasRole(principal, "ROLE_WAREHOUSE_STAFF")) {
			orders = orderRepository.findAllWithDetails();
		} else if (hasRole(principal, "ROLE_CUSTOMER")) {
			orders = orderRepository.findByCustomerIdWithDetails(principal.getId());
		} else {
			throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
		}

		return orders.stream()
				.sorted(Comparator.comparing(Order::getCreatedAt).reversed())
				.map(OrderMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrder(Long orderId, UserPrincipal principal) {
		Order order = orderRepository.findByIdWithDetails(orderId)
				.orElseThrow(() -> new ApiException("Order not found: " + orderId, HttpStatus.NOT_FOUND));

		assertCanView(order, principal);
		return OrderMapper.toResponse(order);
	}

	@Transactional
	public OrderResponse updateStatus(Long orderId, OrderStatus newStatus, UserPrincipal principal) {
		Order order = orderRepository.findByIdWithDetails(orderId)
				.orElseThrow(() -> new ApiException("Order not found: " + orderId, HttpStatus.NOT_FOUND));

		OrderStatus current = order.getStatus();
		assertCanUpdateStatus(principal, current, newStatus);

		Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
		if (!allowed.contains(newStatus)) {
			throw new ApiException(
					"Invalid status transition: " + current + " → " + newStatus,
					HttpStatus.BAD_REQUEST);
		}

		if (newStatus == OrderStatus.CANCELLED) {
			releaseReservations(order);
		} else if (newStatus == OrderStatus.SHIPPED) {
			consumeReservations(order);
		}

		order.setStatus(newStatus);
		Order saved = orderRepository.save(order);

		orderEventService.onOrderStatusChanged(saved.getOrderNumber(), current.name(), newStatus.name());
		return OrderMapper.toResponse(saved);
	}

	private void releaseReservations(Order order) {
		for (OrderItem item : order.getItems()) {
			Inventory inventory = inventoryRepository
					.findForUpdate(item.getProduct().getId(), item.getWarehouse().getId())
					.orElseThrow(() -> new ApiException(
							"Inventory missing for product " + item.getProduct().getSku(),
							HttpStatus.CONFLICT));

			if (inventory.getQuantityReserved() < item.getQuantity()) {
				throw new ApiException(
						"Cannot cancel: reserved quantity mismatch for " + item.getProduct().getSku(),
						HttpStatus.CONFLICT);
			}
			inventory.setQuantityReserved(inventory.getQuantityReserved() - item.getQuantity());
			inventory.setQuantityAvailable(inventory.getQuantityAvailable() + item.getQuantity());
			inventoryRepository.save(inventory);
		}
	}

	private void consumeReservations(Order order) {
		for (OrderItem item : order.getItems()) {
			Inventory inventory = inventoryRepository
					.findForUpdate(item.getProduct().getId(), item.getWarehouse().getId())
					.orElseThrow(() -> new ApiException(
							"Inventory missing for product " + item.getProduct().getSku(),
							HttpStatus.CONFLICT));

			if (inventory.getQuantityReserved() < item.getQuantity()) {
				throw new ApiException(
						"Cannot ship: reserved quantity mismatch for " + item.getProduct().getSku(),
						HttpStatus.CONFLICT);
			}
			inventory.setQuantityReserved(inventory.getQuantityReserved() - item.getQuantity());
			inventoryRepository.save(inventory);
		}
	}

	private void assertCanView(Order order, UserPrincipal principal) {
		if (hasRole(principal, "ROLE_ADMIN") || hasRole(principal, "ROLE_WAREHOUSE_STAFF")) {
			return;
		}
		if (hasRole(principal, "ROLE_CUSTOMER") && order.getCustomer().getId().equals(principal.getId())) {
			return;
		}
		throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
	}

	private void assertCanUpdateStatus(UserPrincipal principal, OrderStatus current, OrderStatus next) {
		if (hasRole(principal, "ROLE_ADMIN")) {
			return;
		}
		if (hasRole(principal, "ROLE_WAREHOUSE_STAFF")) {
			Set<OrderStatus> warehouseStatuses = EnumSet.of(
					OrderStatus.PACKED, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
			if (!warehouseStatuses.contains(next)) {
				throw new ApiException(
						"Warehouse staff can only set PACKED, SHIPPED, or DELIVERED",
						HttpStatus.FORBIDDEN);
			}
			return;
		}
		if (hasRole(principal, "ROLE_CUSTOMER") && next == OrderStatus.CANCELLED) {
			if (current == OrderStatus.SHIPPED
					|| current == OrderStatus.DELIVERED
					|| current == OrderStatus.RETURNED) {
				throw new ApiException(
						"Orders cannot be cancelled after shipping",
						HttpStatus.BAD_REQUEST);
			}
			return;
		}
		throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
	}

	private boolean hasRole(UserPrincipal principal, String role) {
		return principal.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals(role));
	}
}
