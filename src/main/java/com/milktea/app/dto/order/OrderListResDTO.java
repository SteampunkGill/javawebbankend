// File: milktea-backend/src/main/java/com.milktea.app/dto/order/OrderListResDTO.java
package com.milktea.app.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResDTO {
    private List<OrderSummaryDTO> orders;
    private Integer total;
    private Integer page;
    private Integer limit;
    private OrderStatsDTO stats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryDTO {
        private Long id;
        private String orderNo;
        private String status;
        private String statusText;
        private String type;
        private BigDecimal totalAmount;
        private BigDecimal payAmount;
        private Integer itemCount;
        private List<OrderItemBriefDTO> items;
        private String storeName;
        private String address;
        private Instant deliveryTime;
        private Instant createdAt;
        private Boolean needAction;
        private List<String> actions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemBriefDTO {
        private String productName;
        private String productImage;
        private Integer quantity;
        private BigDecimal price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatsDTO {
        private Integer all;
        private Integer pending;
        private Integer paid;
        private Integer making;
        private Integer ready;
        private Integer completed;
        private Integer cancelled;
    }
}