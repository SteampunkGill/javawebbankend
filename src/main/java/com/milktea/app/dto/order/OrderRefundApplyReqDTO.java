// File: milktea-backend/src/main/java/com.milktea.app/dto/order/OrderRefundApplyReqDTO.java
package com.milktea.app.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundApplyReqDTO {
    @NotBlank(message = "退款原因不能为空")
    private String reason;
    private String description;
    private List<String> images;
}