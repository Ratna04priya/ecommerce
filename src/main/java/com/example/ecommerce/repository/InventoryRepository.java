package com.example.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.entity.Inventory;

import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

	Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

	List<Inventory> findByProductId(Long productId);

	List<Inventory> findByWarehouseId(Long warehouseId);

	@Query("""
			select i from Inventory i
			where i.product.id = :productId
			  and i.quantityAvailable > 0
			order by i.quantityAvailable desc
			""")
	List<Inventory> findAvailableByProductId(@Param("productId") Long productId);

	/**
	 * Pessimistic lock for checkout reservation under concurrent purchases.
	 * Complements {@code @Version} optimistic locking on the entity.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Inventory i where i.product.id = :productId and i.warehouse.id = :warehouseId")
	Optional<Inventory> findForUpdate(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select i from Inventory i
			where i.product.id = :productId
			  and i.quantityAvailable > 0
			order by i.quantityAvailable desc
			""")
	List<Inventory> findAvailableByProductIdForUpdate(@Param("productId") Long productId);

	@Query("""
			select coalesce(sum(i.quantityAvailable), 0)
			from Inventory i
			where i.product.id = :productId
			""")
	int sumAvailableByProductId(@Param("productId") Long productId);
}
