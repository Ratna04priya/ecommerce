package com.example.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public List<ProductResponse> getAll(String category, Boolean activeOnly) {
		List<Product> products;

		boolean onlyActive = activeOnly == null || activeOnly;
		if (category != null && !category.isBlank()) {
			products = onlyActive
					? productRepository.findByActiveTrueAndCategoryIgnoreCase(category.trim())
					: productRepository.findByCategoryIgnoreCase(category.trim());
		} else if (onlyActive) {
			products = productRepository.findByActiveTrue();
		} else {
			products = productRepository.findAll();
		}

		return products.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public ProductResponse getById(Long id) {
		return toResponse(findProduct(id));
	}

	@Transactional
	public ProductResponse create(ProductRequest request) {
		String sku = request.getSku().trim().toUpperCase();
		if (productRepository.existsBySku(sku)) {
			throw new ApiException("Product SKU already exists: " + sku, HttpStatus.CONFLICT);
		}

		Product product = Product.builder()
				.name(request.getName().trim())
				.description(trimToNull(request.getDescription()))
				.sku(sku)
				.category(request.getCategory().trim())
				.price(request.getPrice())
				.active(request.getActive() == null || request.getActive())
				.build();

		return toResponse(productRepository.save(product));
	}

	@Transactional
	public ProductResponse update(Long id, ProductRequest request) {
		Product product = findProduct(id);
		String sku = request.getSku().trim().toUpperCase();

		if (!product.getSku().equalsIgnoreCase(sku) && productRepository.existsBySku(sku)) {
			throw new ApiException("Product SKU already exists: " + sku, HttpStatus.CONFLICT);
		}

		product.setName(request.getName().trim());
		product.setDescription(trimToNull(request.getDescription()));
		product.setSku(sku);
		product.setCategory(request.getCategory().trim());
		product.setPrice(request.getPrice());
		if (request.getActive() != null) {
			product.setActive(request.getActive());
		}

		return toResponse(productRepository.save(product));
	}

	@Transactional
	public void delete(Long id) {
		Product product = findProduct(id);
		// Soft delete keeps order history intact
		product.setActive(false);
		productRepository.save(product);
	}

	private Product findProduct(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ApiException("Product not found: " + id, HttpStatus.NOT_FOUND));
	}

	private ProductResponse toResponse(Product product) {
		return ProductResponse.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.sku(product.getSku())
				.category(product.getCategory())
				.price(product.getPrice())
				.active(product.isActive())
				.createdAt(product.getCreatedAt())
				.updatedAt(product.getUpdatedAt())
				.build();
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
