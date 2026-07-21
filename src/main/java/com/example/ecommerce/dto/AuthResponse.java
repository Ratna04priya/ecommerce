package com.example.ecommerce.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

	private String token;
	private String tokenType;
	private Long userId;
	private String email;
	private String fullName;
	private Set<String> roles;
}
