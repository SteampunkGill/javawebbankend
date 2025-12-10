// File: milktea-backend/src/main/java/com.milktea.app/dto/order/PaymentStatusResDTO.java
package com.milktea.app.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResDTO {
    private Long orderId;
    private String payStatus; // unpaid, paid, failed
    private Instant payTime;
    private BigDecimal payAmount;
    private String transactionId;
}