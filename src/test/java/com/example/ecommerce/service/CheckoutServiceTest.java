package com.example.ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.RoleName;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.Warehouse;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.DiscountRepository;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

	@Mock
	private CartRepository cartRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private InventoryRepository inventoryRepository;
	@Mock
	private OrderRepository orderRepository;
	@Mock
	private DiscountRepository discountRepository;
	@Mock
	private CartService cartService;
	@Mock
	private OrderEventService orderEventService;

	@InjectMocks
	private CheckoutService checkoutService;

	private User customer;
	private Product product;
	private Warehouse warehouse;
	private Cart cart;

	@BeforeEach
	void setUp() {
		customer = User.builder()
				.id(10L)
				.email("customer@shop.com")
				.fullName("Demo Customer")
				.password("x")
				.roles(Set.of(Role.builder().id(1L).name(RoleName.CUSTOMER).build()))
				.build();

		product = Product.builder()
				.id(1L)
				.name("Mouse")
				.sku("MOUSE-1")
				.category("Electronics")
				.price(new BigDecimal("20.00"))
				.active(true)
				.build();

		warehouse = Warehouse.builder()
				.id(1L)
				.name("Mumbai")
				.code("MUM-01")
				.location("Mumbai")
				.active(true)
				.build();

		CartItem item = CartItem.builder()
				.id(1L)
				.product(product)
				.quantity(2)
				.build();

		cart = Cart.builder()
				.id(1L)
				.user(customer)
				.items(new ArrayList<>(List.of(item)))
				.build();
		item.setCart(cart);
	}

	@Test
	void checkout_reservesInventoryAndCreatesOrder() {
		Inventory inventory = Inventory.builder()
				.id(1L)
				.product(product)
				.warehouse(warehouse)
				.quantityAvailable(5)
				.quantityReserved(0)
				.build();

		CheckoutRequest request = new CheckoutRequest();
		request.setShippingAddress("12 MG Road");
		request.setPaymentMethod("CARD");

		when(cartRepository.findByUserIdWithItems(10L)).thenReturn(Optional.of(cart));
		when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
		when(inventoryRepository.findAvailableByProductIdForUpdate(1L)).thenReturn(List.of(inventory));
		when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
			Order order = inv.getArgument(0);
			order.setId(100L);
			return order;
		});
		when(orderRepository.findByIdWithDetails(100L)).thenAnswer(inv -> {
			Order saved = Order.builder()
					.id(100L)
					.orderNumber("ORD-TEST")
					.customer(customer)
					.status(OrderStatus.CONFIRMED)
					.subtotal(new BigDecimal("40.00"))
					.taxAmount(new BigDecimal("4.00"))
					.discountAmount(BigDecimal.ZERO)
					.totalAmount(new BigDecimal("44.00"))
					.shippingAddress("12 MG Road")
					.items(new ArrayList<>())
					.build();
			return Optional.of(saved);
		});

		OrderResponse response = checkoutService.checkout(10L, request);

		assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
		assertThat(inventory.getQuantityAvailable()).isEqualTo(3);
		assertThat(inventory.getQuantityReserved()).isEqualTo(2);
		verify(cartService).clearCart(10L);
		verify(orderEventService).onOrderPlaced(anyString(), anyLong(), anyString());
	}

	@Test
	void checkout_insufficientStock_throwsConflict() {
		Inventory inventory = Inventory.builder()
				.id(1L)
				.product(product)
				.warehouse(warehouse)
				.quantityAvailable(1)
				.quantityReserved(0)
				.build();

		CheckoutRequest request = new CheckoutRequest();
		request.setShippingAddress("12 MG Road");

		when(cartRepository.findByUserIdWithItems(10L)).thenReturn(Optional.of(cart));
		when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
		when(inventoryRepository.findAvailableByProductIdForUpdate(1L)).thenReturn(List.of(inventory));

		assertThatThrownBy(() -> checkoutService.checkout(10L, request))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Insufficient stock");
	}

	@Test
	void checkout_emptyCart_throwsBadRequest() {
		Cart empty = Cart.builder().id(1L).user(customer).items(new ArrayList<>()).build();
		CheckoutRequest request = new CheckoutRequest();
		request.setShippingAddress("12 MG Road");

		when(cartRepository.findByUserIdWithItems(10L)).thenReturn(Optional.of(empty));

		assertThatThrownBy(() -> checkoutService.checkout(10L, request))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Cart is empty");
	}
}
