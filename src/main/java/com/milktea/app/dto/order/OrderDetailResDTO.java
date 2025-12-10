// File: milktea-backend/src/main/java/com.milktea.app/dto/order/OrderDetailResDTO.java
package com.milktea.app.dto.order;

import com.milktea.app.dto.coupon.CouponListResDTO;
import com.milktea.app.dto.home.HomePageResDTO;
import com.milktea.app.dto.user.UserAddressResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResDTO {
    private Long id;
    private String orderNo;
    private String status;
    private String statusText;
    private List<OrderStatusTimelineDTO> statusTimeline;
    private String type;
    private List<OrderItemDetailDTO> items;
    private OrderSummaryDetailDTO summary;
    private UserAddressResDTO.AddressDTO address;
    private HomePageResDTO.NearbyStoreDTO store;
    private DeliveryInfoDTO deliveryInfo;
    private PickupInfoDTO pickupInfo;
    private PaymentDetailDTO payment;
    private CouponListResDTO.CouponDTO coupon;
    private Integer pointsUsed;
    private BigDecimal balanceUsed;
    private String remark;
    private InvoiceDetailDTO invoice;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> actions;
    private Instant cancelDeadline;
    private Instant refundDeadline;
    private Instant rateDeadline;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusTimelineDTO {
        private String status;
        private String text;
        private Instant time;
        private Boolean completed;
        private Boolean current;
        private Instant estimatedTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetailDTO {
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private CustomizationsDetailDTO customizations;
        private BigDecimal subtotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomizationsDetailDTO {
        private String sweetness;
        private String temperature;
        private List<ToppingDetailDTO> toppings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingDetailDTO {
        private String name;
        private BigDecimal price;
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryDetailDTO {
        private BigDecimal productAmount;
        private BigDecimal deliveryFee;
        private BigDecimal packageFee;
        private BigDecimal discount;
        private Integer pointsDiscount;
        private BigDecimal balanceDiscount;
        private BigDecimal totalAmount;
        private BigDecimal payAmount;
        private Integer pointsEarned;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInfoDTO {
        private Instant deliveryTime;
        private Instant estimatedArrival;
        private String riderName;
        private String riderPhone;
        private RiderLocationDTO riderLocation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiderLocationDTO {
        private BigDecimal longitude;
        private BigDecimal latitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PickupInfoDTO {
        private String pickupCode;
        private Instant pickupTime;
        private Instant estimatedReadyTime;
        private String counterNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetailDTO {
        private String payType;
        private BigDecimal payAmount;
        private Instant payTime;
        private String transactionId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceDetailDTO {
        private String type;
        private String title;
        private String status;
    }
}