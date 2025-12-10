// File: milktea-backend/src/main/java/com.milktea.app/dto/product/ProductDetailResDTO.java
package com.milktea.app.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResDTO {
    private Long id;
    private String name;
    private String subtitle;
    private List<String> images;
    private String mainImage;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String unit;
    private Integer stock;
    private Integer sales;
    private Integer monthlySales;
    private BigDecimal rating;
    private Integer ratingCount;
    private Integer favoriteCount;
    private Boolean isHot;
    private Boolean isNew;
    private Boolean isRecommend;
    private List<String> tags;
    private String description;
    private String detailHtml;
    private CustomizationsDTO customizations;
    private List<NutritionDTO> nutrition;
    private List<String> ingredients;
    private List<String> allergens;
    private String storage;
    private String shelfLife;
    private List<RelatedProductDTO> relatedProducts;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomizationsDTO {
        private CustomizationTypeDTO sweetness;
        private CustomizationTypeDTO temperature;
        private ToppingsCustomizationDTO toppings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomizationTypeDTO {
        private Boolean enabled;
        private Boolean required;
        private List<OptionDTO> options;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingsCustomizationDTO {
        private Boolean enabled;
        private Boolean required;
        private Integer max;
        private List<ToppingOptionDTO> options;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        private String value;
        private String label;
        private BigDecimal price;
        private Boolean isDefault;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingOptionDTO {
        private String id;
        private String name;
        private BigDecimal price;
        private Integer stock;
        private String icon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionDTO {
        private String name;
        private String value;
        private String unit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedProductDTO {
        private Long id;
        private String name;
        private String image;
        private BigDecimal price;
    }
}