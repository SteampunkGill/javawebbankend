// File: milktea-backend/src/main/java/com.milktea.app/dto/order/PaymentResDTO.java
package com.milktea.app.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResDTO {
    private String payId;
    private String orderNo;
    private BigDecimal payAmount;
    private String payType;
    private Map<String, String> payParams; // Varies by payment gateway
    private Boolean alipaySandbox; // Specific for Alipay
    private String paymentUrl; // For H5 payments
}