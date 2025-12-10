// File: milktea-backend/src/main/java/com.milktea.app/dto/order/OrderCreateResDTO.java
package com.milktea.app.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResDTO {
    private Long orderId;
    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer pointsUsed;
    private BigDecimal balanceUsed;
    private BigDecimal couponAmount;
    private Boolean needPay;
    private PayInfoDTO payInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayInfoDTO {
        private String payId;
        private String payType;
        private BigDecimal payAmount;
        private Instant expireTime;
    }
}