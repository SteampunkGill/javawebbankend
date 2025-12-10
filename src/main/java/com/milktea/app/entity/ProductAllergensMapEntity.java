// File: milktea-backend/src/main/java/com.milktea.app/entity/ProductAllergensMapEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "product_allergens_map")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProductAllergensMapEntity.ProductAllergensMapId.class)
public class ProductAllergensMapEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergen_id", nullable = false)
    private AllergenEntity allergen;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAllergensMapId implements Serializable {
        private Long product;
        private Long allergen;
    }
}