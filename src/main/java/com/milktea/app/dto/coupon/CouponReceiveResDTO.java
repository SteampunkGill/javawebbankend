// File: milktea-backend/src/main/java/com.milktea.app/dto/coupon/CouponReceiveResDTO.java
package com.milktea.app.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponReceiveResDTO {
    private Boolean success;
    private Long couponId;
}