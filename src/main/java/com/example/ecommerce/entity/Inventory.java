package com.example.ecommerce.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "inventory",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_inventory_product_warehouse",
				columnNames = {"product_id", "warehouse_id"}
		)
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "warehouse_id", nullable = false)
	private Warehouse warehouse;

	/** Units physically available for sale (not yet reserved). */
	@Column(nullable = false)
	@Builder.Default
	private int quantityAvailable = 0;

	/** Units reserved for placed/confirmed orders awaiting fulfillment. */
	@Column(nullable = false)
	@Builder.Default
	private int quantityReserved = 0;

	/** Optimistic lock to prevent overselling under concurrent checkouts. */
	@Version
	private Long version;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public int getSellableQuantity() {
		return quantityAvailable;
	}
}
