package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

	@NotBlank
	@Size(max = 500)
	private String shippingAddress;

	@Size(max = 40)
	private String paymentMethod;

	@Size(max = 40)
	private String discountCode;
}
