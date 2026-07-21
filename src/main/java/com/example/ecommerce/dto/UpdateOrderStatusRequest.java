package com.example.ecommerce.dto;

import com.example.ecommerce.entity.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {

	@NotNull
	private OrderStatus status;
}
