// File: milktea-backend/src/main/java/com.milktea.app/entity/ProductIngredientsMapEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "product_ingredients_map")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProductIngredientsMapEntity.ProductIngredientsMapId.class)
public class ProductIngredientsMapEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private IngredientEntity ingredient;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductIngredientsMapId implements Serializable {
        private Long product;
        private Long ingredient;
    }
}