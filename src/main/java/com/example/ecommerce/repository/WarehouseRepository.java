package com.example.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

	Optional<Warehouse> findByCode(String code);

	boolean existsByCode(String code);

	boolean existsByName(String name);

	List<Warehouse> findByActiveTrue();
}
