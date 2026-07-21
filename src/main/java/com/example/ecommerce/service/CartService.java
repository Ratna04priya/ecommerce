package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final InventoryRepository inventoryRepository;

	@Transactional(readOnly = true)
	public CartResponse getCart(Long userId) {
		Cart cart = cartRepository.findByUserIdWithItems(userId)
				.orElseGet(() -> emptyCartView(userId));
		return toResponse(cart);
	}

	@Transactional
	public CartResponse addItem(Long userId, AddToCartRequest request) {
		Product product = productRepository.findById(request.getProductId())
				.orElseThrow(() -> new ApiException(
						"Product not found: " + request.getProductId(), HttpStatus.NOT_FOUND));

		if (!product.isActive()) {
			throw new ApiException("Product is not available: " + product.getSku(), HttpStatus.BAD_REQUEST);
		}

		int available = inventoryRepository.sumAvailableByProductId(product.getId());
		Cart cart = getOrCreateCart(userId);

		CartItem existing = cartItemRepository
				.findByCartIdAndProductId(cart.getId(), product.getId())
				.orElse(null);

		int newQuantity = request.getQuantity() + (existing != null ? existing.getQuantity() : 0);
		if (newQuantity > available) {
			throw new ApiException(
					"Insufficient stock for " + product.getSku() + ". Available: " + available,
					HttpStatus.BAD_REQUEST);
		}

		if (existing != null) {
			existing.setQuantity(newQuantity);
			cartItemRepository.save(existing);
		} else {
			CartItem item = CartItem.builder()
					.cart(cart)
					.product(product)
					.quantity(request.getQuantity())
					.build();
			cart.getItems().add(item);
			cartItemRepository.save(item);
		}

		return toResponse(cartRepository.findByUserIdWithItems(userId).orElseThrow());
	}

	@Transactional
	public CartResponse removeItem(Long userId, Long cartItemId) {
		CartItem item = cartItemRepository.findByIdAndCartUserId(cartItemId, userId)
				.orElseThrow(() -> new ApiException(
						"Cart item not found: " + cartItemId, HttpStatus.NOT_FOUND));

		Cart cart = item.getCart();
		cart.getItems().remove(item);
		cartItemRepository.delete(item);

		return toResponse(cartRepository.findByUserIdWithItems(userId).orElseThrow());
	}

	@Transactional
	public void clearCart(Long userId) {
		cartRepository.findByUserId(userId).ifPresent(cart -> {
			cart.getItems().clear();
			cartRepository.save(cart);
		});
	}

	private Cart getOrCreateCart(Long userId) {
		return cartRepository.findByUserId(userId).orElseGet(() -> {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
			return cartRepository.save(Cart.builder().user(user).items(new ArrayList<>()).build());
		});
	}

	private Cart emptyCartView(Long userId) {
		return Cart.builder()
				.id(null)
				.user(User.builder().id(userId).build())
				.items(new ArrayList<>())
				.build();
	}

	private CartResponse toResponse(Cart cart) {
		List<CartItemResponse> items = cart.getItems().stream()
				.map(item -> {
					BigDecimal unitPrice = item.getProduct().getPrice();
					BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
					return CartItemResponse.builder()
							.id(item.getId())
							.productId(item.getProduct().getId())
							.productName(item.getProduct().getName())
							.productSku(item.getProduct().getSku())
							.unitPrice(unitPrice)
							.quantity(item.getQuantity())
							.lineTotal(lineTotal)
							.build();
				})
				.toList();

		BigDecimal subtotal = items.stream()
				.map(CartItemResponse::getLineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		int totalItems = items.stream().mapToInt(CartItemResponse::getQuantity).sum();

		return CartResponse.builder()
				.cartId(cart.getId())
				.userId(cart.getUser().getId())
				.items(items)
				.totalItems(totalItems)
				.subtotal(subtotal)
				.build();
	}
}
