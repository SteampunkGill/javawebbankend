// File: milktea-backend/src/main/java/com.milktea.app/dto/order/CheckoutReqDTO.java
package com.milktea.app.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutReqDTO {
    @NotEmpty(message = "购物车项ID列表不能为空")
    private List<Long> itemIds;
    private Long addressId;
    private Long couponId;
    private String remark;
}