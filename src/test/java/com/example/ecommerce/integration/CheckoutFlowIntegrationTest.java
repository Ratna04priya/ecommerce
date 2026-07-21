package com.example.ecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CheckoutFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void fullCheckoutAndFulfillmentFlow() throws Exception {
		String adminToken = login("admin@shop.com", "admin123");
		String customerToken = login("customer@shop.com", "customer123");
		String warehouseToken = login("warehouse@shop.com", "warehouse123");

		MvcResult productResult = mockMvc.perform(post("/api/products")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "USB-C Hub",
								  "description": "7-in-1 hub",
								  "sku": "HUB-100",
								  "category": "Electronics",
								  "price": 49.99,
								  "active": true
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andReturn();

		long productId = objectMapper.readTree(productResult.getResponse().getContentAsString()).get("id").asLong();

		MvcResult warehouseResult = mockMvc.perform(post("/api/warehouses")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Delhi Hub",
								  "code": "DEL-01",
								  "location": "Delhi, IN",
								  "active": true
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		long warehouseId = objectMapper.readTree(warehouseResult.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(put("/api/inventory")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "productId": %d,
								  "warehouseId": %d,
								  "quantityAvailable": 20
								}
								""".formatted(productId, warehouseId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantityAvailable").value(20));

		mockMvc.perform(post("/api/cart/add")
						.header("Authorization", "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "productId": %d,
								  "quantity": 2
								}
								""".formatted(productId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(2));

		MvcResult checkoutResult = mockMvc.perform(post("/api/checkout")
						.header("Authorization", "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "shippingAddress": "221B Baker Street",
								  "paymentMethod": "CARD",
								  "discountCode": "SAVE10"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("CONFIRMED"))
				.andExpect(jsonPath("$.payment.status").value("SUCCESS"))
				.andExpect(jsonPath("$.items[0].warehouseId").value(warehouseId))
				.andReturn();

		JsonNode order = objectMapper.readTree(checkoutResult.getResponse().getContentAsString());
		long orderId = order.get("id").asLong();
		assertThat(order.get("discountAmount").decimalValue()).isPositive();

		mockMvc.perform(get("/api/cart")
						.header("Authorization", "Bearer " + customerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(0));

		mockMvc.perform(put("/api/orders/%d/status".formatted(orderId))
						.header("Authorization", "Bearer " + warehouseToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "status": "PACKED" }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PACKED"));

		mockMvc.perform(put("/api/orders/%d/status".formatted(orderId))
						.header("Authorization", "Bearer " + warehouseToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "status": "SHIPPED" }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SHIPPED"));

		mockMvc.perform(put("/api/orders/%d/status".formatted(orderId))
						.header("Authorization", "Bearer " + warehouseToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "status": "DELIVERED" }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DELIVERED"));

		mockMvc.perform(post("/api/returns")
						.header("Authorization", "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "orderId": %d,
								  "reason": "Defective unit"
								}
								""".formatted(orderId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("REFUNDED"))
				.andExpect(jsonPath("$.refundReference").isNotEmpty());

		mockMvc.perform(get("/api/orders/%d".formatted(orderId))
						.header("Authorization", "Bearer " + customerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("RETURNED"))
				.andExpect(jsonPath("$.payment.status").value("REFUNDED"));
	}

	private String login(String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
	}
}
