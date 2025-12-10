package com.milktea.app.dto.home;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class HomePageResDTO {

    // Banner 相关
    private List<BannerDTO> banners;

    // 快捷入口
    private List<QuickEntryDTO> quickEntries;

    // 推荐商品
    private RecommendProductsDTO recommendProducts;

    // 分类
    private List<CategoryDTO> categories;

    // 热销商品
    private List<HotProductDTO> hotProducts;

    // 促销活动
    private List<PromotionDTO> promotions;

    // 附近门店
    private NearbyStoreDTO nearbyStore;

    // ========== 内部类定义 ==========

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BannerDTO {
        private String id;
        private String image;
        private String title;
        private String subtitle;
        private String type;
        private String targetId;
        private String url;
        private String backgroundColor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickEntryDTO {
        private String id;
        private String icon;
        private String name;
        private String type;
        private String targetId;
        private String badge;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendProductsDTO {
        private String title;
        private List<ProductItemDTO> products;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItemDTO {
        private String id;
        private String name;
        private String image;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer sales;
        private List<String> tags;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDTO {
        private String id;
        private String name;
        private String icon;
        private String image;
        private Integer productCount;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotProductDTO {
        private String id;
        private Integer rank;
        private String name;
        private String image;
        private BigDecimal price;
        private BigDecimal rating;
        private Integer monthlySales;
        private BigDecimal increaseRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionDTO {
        private String id;
        private String title;
        private String subtitle;
        private String image;
        private String type;
        private BigDecimal value;
        private Instant startTime;
        private Instant endTime;
        private String buttonText;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearbyStoreDTO {
        private String id;
        private String name;
        private String address;
        private Integer distance;
        private String businessHours;
        private String status;
        private String phone;
        private List<String> services;
        private List<String> tags;
        private BigDecimal deliveryFee;
        private BigDecimal minimumOrderAmount;
        private BigDecimal rating;
        private List<String> images;
        private Integer currentWaitTime;
        private Boolean isFavorite;
        private BigDecimal longitude;
        private BigDecimal latitude;
    }
}