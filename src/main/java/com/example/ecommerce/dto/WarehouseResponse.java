package com.example.ecommerce.dto;

import java.time.Instant;

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
public class WarehouseResponse {

	private Long id;
	private String name;
	private String code;
	private String location;
	private boolean active;
	private Instant createdAt;
}
