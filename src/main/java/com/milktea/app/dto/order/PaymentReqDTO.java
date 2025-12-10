// File: milktea-backend/src/main/java/com.milktea.app/dto/order/PaymentReqDTO.java
package com.milktea.app.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReqDTO {
    @NotBlank(message = "支付方式不能为空")
    private String payType; // alipay, wechat
    @NotBlank(message = "支付渠道不能为空")
    private String channel; // miniprogram, app, h5
}