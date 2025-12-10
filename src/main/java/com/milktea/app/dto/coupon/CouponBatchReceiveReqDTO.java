// File: milktea-backend/src/main/java/com.milktea.app/dto/coupon/CouponBatchReceiveReqDTO.java
package com.milktea.app.dto.coupon;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponBatchReceiveReqDTO {
    @NotEmpty(message = "优惠券ID列表不能为空")
    private List<Long> couponIds;
}