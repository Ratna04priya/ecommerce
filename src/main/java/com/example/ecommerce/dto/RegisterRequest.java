package com.example.ecommerce.dto;

import com.example.ecommerce.entity.RoleName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(min = 6, max = 100)
	private String password;

	@NotBlank
	@Size(min = 2, max = 120)
	private String fullName;

	/**
	 * Optional. Defaults to CUSTOMER when omitted.
	 * Allowed values: ADMIN, CUSTOMER, WAREHOUSE_STAFF
	 */
	private RoleName role;
}
