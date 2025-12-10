// File: milktea-backend/src/main/java/com.milktea.app/controller/CouponV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.common.util.PaginationUtil;
import com.milktea.app.dto.coupon.CouponBatchReceiveReqDTO;
import com.milktea.app.dto.coupon.CouponListResDTO;
import com.milktea.app.dto.coupon.CouponReceiveResDTO;
import com.milktea.app.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/coupons") // Base path for coupons module
@RequiredArgsConstructor
@Slf4j
public class CouponV1Controller {

    private final CouponService couponService;

    private Long getUserId(@AuthenticationPrincipal User principal) {
        return Long.parseLong(principal.getUsername());
    }

    @GetMapping // Matches /coupons
    public ApiResponse<CouponListResDTO> getUserCoupons(@AuthenticationPrincipal User principal,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = getUserId(principal);
        log.info("Getting coupons for user {} with status {}", userId, status);
        Pageable pageable = PaginationUtil.createPageable(page, limit);
        CouponListResDTO resDTO = couponService.getUserCoupons(userId, status, pageable);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/{templateId}/receive") // Matches /coupons/{id}/receive
    public ApiResponse<CouponReceiveResDTO> receiveCoupon(@AuthenticationPrincipal User principal,
                                                          @PathVariable("templateId") Long templateId) { // Renamed path variable for clarity
        Long userId = getUserId(principal);
        log.info("User {} receiving coupon template {}", userId, templateId);
        CouponReceiveResDTO resDTO = couponService.receiveCoupon(userId, templateId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/batchreceive") // Matches /coupons/batchreceive
    public ApiResponse<CouponReceiveResDTO> batchReceiveCoupons(@AuthenticationPrincipal User principal,
                                                                @Valid @RequestBody CouponBatchReceiveReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("User {} batch receiving coupons: {}", userId, reqDTO.getCouponIds());
        // Note: The response for batch operations might be more detailed in a real app (e.g., list of successful/failed IDs)
        CouponReceiveResDTO resDTO = couponService.batchReceiveCoupons(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }
}