package com.example.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.DiscountRequest;
import com.example.ecommerce.dto.DiscountResponse;
import com.example.ecommerce.entity.Discount;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.DiscountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountService {

	private final DiscountRepository discountRepository;

	@Transactional(readOnly = true)
	public List<DiscountResponse> listAll() {
		return discountRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Transactional
	public DiscountResponse create(DiscountRequest request) {
		String code = request.getCode().trim().toUpperCase();
		if (discountRepository.findByCodeIgnoreCase(code).isPresent()) {
			throw new ApiException("Discount code already exists: " + code, HttpStatus.CONFLICT);
		}

		Discount discount = Discount.builder()
				.code(code)
				.percentOff(request.getPercentOff())
				.active(request.getActive() == null || request.getActive())
				.expiresAt(request.getExpiresAt())
				.build();

		return toResponse(discountRepository.save(discount));
	}

	private DiscountResponse toResponse(Discount discount) {
		return DiscountResponse.builder()
				.id(discount.getId())
				.code(discount.getCode())
				.percentOff(discount.getPercentOff())
				.active(discount.isActive())
				.expiresAt(discount.getExpiresAt())
				.createdAt(discount.getCreatedAt())
				.build();
	}
}
