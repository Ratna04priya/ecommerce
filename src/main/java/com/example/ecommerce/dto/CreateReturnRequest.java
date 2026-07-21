package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReturnRequest {

	@NotNull
	private Long orderId;

	@NotBlank
	@Size(max = 500)
	private String reason;
}
