// File: milktea-backend/src/main/java/com.milktea.app/service/CouponService.java
package com.milktea.app.service;

import com.milktea.app.dto.coupon.CouponBatchReceiveReqDTO;
import com.milktea.app.dto.coupon.CouponListResDTO;
import com.milktea.app.dto.coupon.CouponReceiveResDTO;
import org.springframework.data.domain.Pageable;

public interface CouponService {
    CouponListResDTO getUserCoupons(Long userId, String status, Pageable pageable);
    CouponReceiveResDTO receiveCoupon(Long userId, Long templateId);
    CouponReceiveResDTO batchReceiveCoupons(Long userId, CouponBatchReceiveReqDTO reqDTO);
}