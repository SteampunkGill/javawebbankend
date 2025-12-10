// File: milktea-backend/src/main/java/com.milktea.app/entity/ProductCustomizationOptionEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_customization_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCustomizationOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customization_type_id", nullable = false)
    private ProductCustomizationTypeEntity customizationType;

    @Column(name = "value", nullable = false, length = 50)
    private String value; // no_sugar, no_ice, topping_1

    @Column(name = "label", nullable = false, length = 50)
    private String label; // 无糖, 去冰, 珍珠

    @Column(name = "price_adjustment", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAdjustment;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}