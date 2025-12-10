// File: milktea-backend/src/main/java/com.milktea.app/dto/order/OrderStatusChangedWsDTO.java
package com.milktea.app.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedWsDTO {
    private String type; // Should be "order_status_changed"
    private OrderStatusChangedData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusChangedData {
        private Long orderId;
        private String orderNo;
        private String oldStatus;
        private String newStatus;
        private String statusText;
        private Long timestamp; // Unix timestamp in milliseconds
        private Instant estimatedReadyTime;
    }
}