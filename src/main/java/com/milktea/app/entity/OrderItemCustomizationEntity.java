// File: milktea-backend/src/main/java/com/milktea/app/entity/OrderItemCustomizationEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_item_customizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemCustomizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItemEntity orderItem;

    @Column(name = "customization_type_name", nullable = false, length = 50)
    private String customizationTypeName;

    @Column(name = "option_value", nullable = false, length = 50)
    private String optionValue;

    @Column(name = "option_label", nullable = false, length = 50)
    private String optionLabel;

    @Column(name = "price_adjustment_at_order", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAdjustmentAtOrder;

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