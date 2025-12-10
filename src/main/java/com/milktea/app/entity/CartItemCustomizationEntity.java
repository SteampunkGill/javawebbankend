// File: milktea-backend/src/main/java/com/milktea/app/entity/CartItemCustomizationEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cart_item_customizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemCustomizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id", nullable = false)
    private CartItemEntity cartItem;

    @Column(name = "customization_type_name", nullable = false, length = 50)
    private String customizationTypeName; // sweetness, temperature, toppings

    @Column(name = "option_value", nullable = false, length = 50)
    private String optionValue; // no_sugar, no_ice, topping_1

    @Column(name = "option_label", nullable = false, length = 50)
    private String optionLabel;

    @Column(name = "price_adjustment_at_add", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAdjustmentAtAdd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}