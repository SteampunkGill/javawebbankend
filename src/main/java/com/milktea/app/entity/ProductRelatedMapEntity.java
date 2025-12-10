// File: milktea-backend/src/main/java/com.milktea.app/entity/ProductRelatedMapEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "product_related_map")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProductRelatedMapEntity.ProductRelatedMapId.class)
public class ProductRelatedMapEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_product_id", nullable = false)
    private ProductEntity mainProduct;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_product_id", nullable = false)
    private ProductEntity relatedProduct;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRelatedMapId implements Serializable {
        private Long mainProduct;
        private Long relatedProduct;
    }
}