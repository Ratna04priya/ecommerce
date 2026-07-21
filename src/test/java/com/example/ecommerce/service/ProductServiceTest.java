package com.example.ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	@Test
	void createProduct_success() {
		ProductRequest request = new ProductRequest();
		request.setName("Mouse");
		request.setSku("MOUSE-1");
		request.setCategory("Electronics");
		request.setPrice(new BigDecimal("19.99"));
		request.setDescription("Wireless");

		when(productRepository.existsBySku("MOUSE-1")).thenReturn(false);
		when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
			Product p = invocation.getArgument(0);
			p.setId(1L);
			return p;
		});

		ProductResponse response = productService.create(request);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getSku()).isEqualTo("MOUSE-1");
		assertThat(response.getName()).isEqualTo("Mouse");

		ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
		verify(productRepository).save(captor.capture());
		assertThat(captor.getValue().isActive()).isTrue();
	}

	@Test
	void createProduct_duplicateSku_throwsConflict() {
		ProductRequest request = new ProductRequest();
		request.setName("Mouse");
		request.setSku("MOUSE-1");
		request.setCategory("Electronics");
		request.setPrice(new BigDecimal("19.99"));

		when(productRepository.existsBySku("MOUSE-1")).thenReturn(true);

		assertThatThrownBy(() -> productService.create(request))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("SKU already exists");

		verify(productRepository, never()).save(any());
	}

	@Test
	void getById_notFound_throws() {
		when(productRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.getById(99L))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Product not found");
	}

	@Test
	void delete_softDeletesProduct() {
		Product product = Product.builder()
				.id(5L)
				.name("Keyboard")
				.sku("KB-1")
				.category("Electronics")
				.price(new BigDecimal("49.00"))
				.active(true)
				.build();

		when(productRepository.findById(5L)).thenReturn(Optional.of(product));
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

		productService.delete(5L);

		assertThat(product.isActive()).isFalse();
		verify(productRepository).save(product);
	}
}
