// File: milktea-backend/src/main/java/com.milktea.app/dto/order/OrderCreateReqDTO.java
package com.milktea.app.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateReqDTO {
    @NotBlank(message = "订单类型不能为空")
    private String type; // delivery, pickup
    @NotEmpty(message = "订单商品列表不能为空")
    private List<OrderItemCreateDTO> items;
    private Long addressId; // Required for delivery
    private Long storeId; // Required for pickup
    private Long couponId;
    private Integer points;
    private BigDecimal balance;
    private Instant deliveryTime; // Expected delivery time
    private String remark;
    private InvoiceDTO invoice;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemCreateDTO {
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        @NotNull(message = "商品数量不能为空")
        private Integer quantity;
        private Customization customizations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customization {
        private String sweetness;
        private String temperature;
        private List<ToppingItem> toppings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingItem {
        private String id;
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceDTO {
        @NotBlank(message = "发票类型不能为空")
        private String type; // personal, company
        @NotBlank(message = "发票抬头不能为空")
        private String title;
        private String taxNumber; // Required for company
    }
}