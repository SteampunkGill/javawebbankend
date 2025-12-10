// File: milktea-backend/src/main/java/com.milktea.app/dto/cart/CartResDTO.java
package com.milktea.app.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResDTO {
    private List<CartItemDTO> items;
    private CartSummaryDTO summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer quantity;
        private Integer maxQuantity;
        private Integer stock;
        private CustomizationsDTO customizations;
        private BigDecimal subtotal;
        private Boolean isSelected;
        private Boolean isValid;
        private String invalidReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomizationsDTO {
        private OptionDTO sweetness;
        private OptionDTO temperature;
        private List<ToppingDTO> toppings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        private String value;
        private String label;
        private BigDecimal price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingDTO {
        private String id;
        private String name;
        private BigDecimal price;
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartSummaryDTO {
        private Integer totalQuantity;
        private Integer selectedQuantity;
        private BigDecimal totalAmount;
        private BigDecimal selectedAmount;
        private BigDecimal totalDiscount;
        private BigDecimal deliveryFee;
        private BigDecimal finalAmount;
        private Integer validItemCount;
        private Integer invalidItemCount;
    }
}