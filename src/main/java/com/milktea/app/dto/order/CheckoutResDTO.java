// File: milktea-backend/src/main/java/com.milktea.app/dto/order/CheckoutResDTO.java
package com.milktea.app.dto.order;

import com.milktea.app.dto.coupon.CouponListResDTO;
import com.milktea.app.dto.home.HomePageResDTO;
import com.milktea.app.dto.user.UserAddressResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResDTO {
    private List<OrderItemSummaryDTO> orderItems;
    private UserAddressResDTO.AddressDTO address;
    private List<CouponListResDTO.CouponDTO> availableCoupons;
    private CouponListResDTO.CouponDTO selectedCoupon;
    private OrderSummaryDTO summary;
    private Integer availablePoints;
    private Integer pointsRate;
    private BigDecimal balance;
    private DeliveryTimeDTO deliveryTime;
    private HomePageResDTO.NearbyStoreDTO store;
    private BigDecimal minimumOrderAmount;
    private String remark;
    private List<WarningDTO> warnings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemSummaryDTO {
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private BigDecimal price;
        private CustomizationsDTO customizations;
        private BigDecimal subtotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomizationsDTO {
        private String sweetness;
        private String temperature;
        private List<String> toppings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryDTO {
        private BigDecimal productAmount;
        private BigDecimal deliveryFee;
        private BigDecimal packageFee;
        private BigDecimal discount;
        private BigDecimal totalAmount;
        private BigDecimal pointsDiscount;
        private BigDecimal balanceDiscount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryTimeDTO {
        private LocalDate date;
        private String timeRange;
        private Boolean isToday;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarningDTO {
        private String type;
        private String message;
    }
}