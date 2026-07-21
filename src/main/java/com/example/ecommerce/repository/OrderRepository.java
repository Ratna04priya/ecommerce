package com.example.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	Optional<Order> findByOrderNumber(String orderNumber);

	List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

	@Query("""
			select distinct o from Order o
			left join fetch o.items i
			left join fetch i.product
			left join fetch i.warehouse
			left join fetch o.payment
			where o.id = :id
			""")
	Optional<Order> findByIdWithDetails(@Param("id") Long id);

	@Query("""
			select distinct o from Order o
			left join fetch o.items i
			left join fetch i.product
			left join fetch i.warehouse
			left join fetch o.payment
			where o.orderNumber = :orderNumber
			""")
	Optional<Order> findByOrderNumberWithDetails(@Param("orderNumber") String orderNumber);
}
