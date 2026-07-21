package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Discount;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.PaymentStatus;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.DiscountRepository;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {

	private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

	private final CartRepository cartRepository;
	private final UserRepository userRepository;
	private final InventoryRepository inventoryRepository;
	private final OrderRepository orderRepository;
	private final DiscountRepository discountRepository;
	private final CartService cartService;
	private final OrderEventService orderEventService;

	@Transactional
	public OrderResponse checkout(Long userId, CheckoutRequest request) {
		Cart cart = cartRepository.findByUserIdWithItems(userId)
				.orElseThrow(() -> new ApiException("Cart is empty", HttpStatus.BAD_REQUEST));

		if (cart.getItems().isEmpty()) {
			throw new ApiException("Cart is empty", HttpStatus.BAD_REQUEST);
		}

		User customer = userRepository.findById(userId)
				.orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

		BigDecimal subtotal = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();

		for (CartItem cartItem : cart.getItems()) {
			List<ReservedStock> reservations = reserveStock(
					cartItem.getProduct().getId(),
					cartItem.getQuantity(),
					cartItem.getProduct().getSku());

			BigDecimal unitPrice = cartItem.getProduct().getPrice();
			for (ReservedStock reservation : reservations) {
				BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(reservation.quantity()))
						.setScale(2, RoundingMode.HALF_UP);
				subtotal = subtotal.add(lineTotal);

				orderItems.add(OrderItem.builder()
						.product(cartItem.getProduct())
						.warehouse(reservation.inventory().getWarehouse())
						.quantity(reservation.quantity())
						.unitPrice(unitPrice)
						.lineTotal(lineTotal)
						.build());
			}
		}

		subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
		DiscountResult discountResult = applyDiscount(request.getDiscountCode(), subtotal);
		BigDecimal taxable = subtotal.subtract(discountResult.discountAmount()).max(BigDecimal.ZERO);
		BigDecimal taxAmount = taxable.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
		BigDecimal totalAmount = taxable.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

		String paymentMethod = (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank())
				? "CARD"
				: request.getPaymentMethod().trim().toUpperCase();

		Order order = Order.builder()
				.orderNumber(generateOrderNumber())
				.customer(customer)
				.status(OrderStatus.PLACED)
				.subtotal(subtotal)
				.taxAmount(taxAmount)
				.discountAmount(discountResult.discountAmount())
				.totalAmount(totalAmount)
				.discountCode(discountResult.code())
				.shippingAddress(request.getShippingAddress().trim())
				.items(new ArrayList<>())
				.build();

		for (OrderItem item : orderItems) {
			item.setOrder(order);
			order.getItems().add(item);
		}

		Payment payment = Payment.builder()
				.order(order)
				.amount(totalAmount)
				.status(PaymentStatus.SUCCESS)
				.method(paymentMethod)
				.transactionReference("TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
				.build();
		order.setPayment(payment);

		// Payment succeeded → move to CONFIRMED atomically with checkout
		order.setStatus(OrderStatus.CONFIRMED);

		Order saved = orderRepository.save(order);
		cartService.clearCart(userId);

		orderEventService.onOrderPlaced(
				saved.getOrderNumber(),
				customer.getId(),
				saved.getTotalAmount().toPlainString());

		Order detailed = orderRepository.findByIdWithDetails(saved.getId()).orElse(saved);
		return OrderMapper.toResponse(detailed);
	}

	private List<ReservedStock> reserveStock(Long productId, int requiredQty, String sku) {
		List<Inventory> rows = inventoryRepository.findAvailableByProductIdForUpdate(productId);
		int remaining = requiredQty;
		List<ReservedStock> reserved = new ArrayList<>();

		for (Inventory inventory : rows) {
			if (remaining <= 0) {
				break;
			}
			int take = Math.min(inventory.getQuantityAvailable(), remaining);
			if (take <= 0) {
				continue;
			}
			inventory.setQuantityAvailable(inventory.getQuantityAvailable() - take);
			inventory.setQuantityReserved(inventory.getQuantityReserved() + take);
			inventoryRepository.save(inventory);
			reserved.add(new ReservedStock(inventory, take));
			remaining -= take;
		}

		if (remaining > 0) {
			throw new ApiException(
					"Insufficient stock for " + sku + ". Short by " + remaining,
					HttpStatus.CONFLICT);
		}
		return reserved;
	}

	private DiscountResult applyDiscount(String discountCode, BigDecimal subtotal) {
		if (discountCode == null || discountCode.isBlank()) {
			return new DiscountResult(null, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
		}

		Discount discount = discountRepository.findByCodeIgnoreCase(discountCode.trim())
				.orElseThrow(() -> new ApiException("Invalid discount code", HttpStatus.BAD_REQUEST));

		if (!discount.isActive()) {
			throw new ApiException("Discount code is inactive", HttpStatus.BAD_REQUEST);
		}
		if (discount.getExpiresAt() != null && discount.getExpiresAt().isBefore(Instant.now())) {
			throw new ApiException("Discount code has expired", HttpStatus.BAD_REQUEST);
		}

		BigDecimal amount = subtotal
				.multiply(discount.getPercentOff())
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		return new DiscountResult(discount.getCode(), amount);
	}

	private String generateOrderNumber() {
		return "ORD-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
	}

	private record ReservedStock(Inventory inventory, int quantity) {
	}

	private record DiscountResult(String code, BigDecimal discountAmount) {
	}
}
