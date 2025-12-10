// File: milktea-backend/src/main/java/com.milktea.app/entity/ProductEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "detail_html")
    private String detailHtml;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "sales", nullable = false)
    private Integer sales;

    @Column(name = "monthly_sales", nullable = false)
    private Integer monthlySales;

    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount;

    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount;

    @Column(name = "is_hot", nullable = false)
    private Boolean isHot;

    @Column(name = "is_new", nullable = false)
    private Boolean isNew;

    @Column(name = "is_recommend", nullable = false)
    private Boolean isRecommend;

    @Column(name = "tags")
    @JdbcTypeCode(SqlTypes.JSON) // Use JdbcTypeCode for JSONB mapping
    private String tags; // JSON array of strings, e.g., ["招牌", "人气"]

    @Column(name = "storage_method", length = 100)
    private String storageMethod;

    @Column(name = "shelf_life", length = 50)
    private String shelfLife;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImageEntity> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductCustomizationTypeEntity> customizationTypes;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductNutritionEntity> nutritions;

    @ManyToMany
    @JoinTable(
            name = "product_ingredients_map",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private List<IngredientEntity> ingredients;

    @ManyToMany
    @JoinTable(
            name = "product_allergens_map",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    private List<AllergenEntity> allergens;

    @ManyToMany
    @JoinTable(
            name = "product_related_map",
            joinColumns = @JoinColumn(name = "main_product_id"),
            inverseJoinColumns = @JoinColumn(name = "related_product_id")
    )
    private List<ProductEntity> relatedProducts;

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