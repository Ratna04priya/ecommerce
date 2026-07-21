package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseRequest {

	@NotBlank
	@Size(max = 120)
	private String name;

	@NotBlank
	@Size(max = 40)
	private String code;

	@NotBlank
	@Size(max = 255)
	private String location;

	private Boolean active;
}
