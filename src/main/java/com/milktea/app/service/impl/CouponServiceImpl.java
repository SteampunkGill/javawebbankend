package com.milktea.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.common.util.DateUtil;
import com.milktea.app.dto.coupon.CouponBatchReceiveReqDTO;
import com.milktea.app.dto.coupon.CouponListResDTO;
import com.milktea.app.dto.coupon.CouponReceiveResDTO;
import com.milktea.app.entity.CouponTemplateEntity;
import com.milktea.app.entity.UserCouponEntity;
import com.milktea.app.entity.UserEntity;
import com.milktea.app.repository.CouponTemplateRepository;
import com.milktea.app.repository.UserCouponRepository;
import com.milktea.app.repository.UserRepository;
import com.milktea.app.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponTemplateRepository couponTemplateRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;  // 添加 ObjectMapper

    @Override
    @Transactional(readOnly = true)
    public CouponListResDTO getUserCoupons(Long userId, String status, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        List<UserCouponEntity> userCoupons;
        if (status == null || status.equalsIgnoreCase("all")) {
            // Fetch all coupons for the user, then filter and paginate in memory or refine repository query
            // For simplicity here, we'll assume the repo can handle it or fetch all then filter.
            // A production app would have more specific queries or predicates.
            userCoupons = userCouponRepository.findByUserIdAndStatusOrderByExpireAtAsc(userId, "available"); // Example
            userCoupons.addAll(userCouponRepository.findByUserIdAndStatusOrderByExpireAtAsc(userId, "used"));
            userCoupons.addAll(userCouponRepository.findByUserIdAndStatusOrderByExpireAtAsc(userId, "expired"));
        } else {
            userCoupons = userCouponRepository.findByUserIdAndStatusOrderByExpireAtAsc(userId, status.toLowerCase());
        }

        List<CouponListResDTO.CouponDTO> couponDTOs = userCoupons.stream()
                .map(this::mapToCouponDTO)
                .collect(Collectors.toList());

        // Manual pagination if not using Page directly from JPA
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), couponDTOs.size());
        List<CouponListResDTO.CouponDTO> pagedDTOs = couponDTOs.subList(start, end);
        Page<CouponListResDTO.CouponDTO> couponPage = new PageImpl<>(pagedDTOs, pageable, couponDTOs.size());

        CouponListResDTO resDTO = new CouponListResDTO();
        resDTO.setCoupons(couponPage.getContent());
        resDTO.setTotal((int) couponPage.getTotalElements());
        resDTO.setPage(pageable.getPageNumber() + 1);
        resDTO.setLimit(pageable.getPageSize());
        return resDTO;
    }

    @Override
    @Transactional
    public CouponReceiveResDTO receiveCoupon(Long userId, Long templateId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        CouponTemplateEntity template = couponTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND, "Coupon template not found."));

        if (!template.getIsActive()) {
            throw new BusinessException(ErrorCode.COUPON_NOT_FOUND, "Coupon is not active.");
        }
        if (template.getRemainingQuantity() <= 0) {
            throw new BusinessException(ErrorCode.COUPON_OUT_OF_STOCK, "Coupon has been fully distributed.");
        }

        long acquiredCount = userCouponRepository.countByUserIdAndCouponTemplateIdAndStatus(userId, templateId, "available") +
                userCouponRepository.countByUserIdAndCouponTemplateIdAndStatus(userId, templateId, "used");
        if (acquiredCount >= template.getAcquireLimit()) {
            throw new BusinessException(ErrorCode.COUPON_ACQUIRE_LIMIT_REACHED, "User has reached the acquisition limit for this coupon.");
        }

        UserCouponEntity userCoupon = new UserCouponEntity();
        userCoupon.setUser(user);
        userCoupon.setCouponTemplate(template);
        userCoupon.setStatus("available");
        userCoupon.setReceivedAt(Instant.now());
        userCoupon.setCreatedAt(Instant.now());
        userCoupon.setUpdatedAt(Instant.now());

        // Calculate expire_at based on validity_type
        if ("fixed_days".equalsIgnoreCase(template.getValidityType())) {
            userCoupon.setExpireAt(Instant.now().plus(template.getValidDays(), ChronoUnit.DAYS));
        } else if ("fixed_date_range".equalsIgnoreCase(template.getValidityType())) {
            // Convert LocalDate to Instant, assuming end of day for expiration
            userCoupon.setExpireAt(template.getEndDate().plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant());
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Invalid coupon validity type.");
        }

        template.setRemainingQuantity(template.getRemainingQuantity() - 1); // Decrease remaining quantity
        couponTemplateRepository.save(template);
        userCoupon = userCouponRepository.save(userCoupon);

        return new CouponReceiveResDTO(true, userCoupon.getId());
    }

    @Override
    @Transactional
    public CouponReceiveResDTO batchReceiveCoupons(Long userId, CouponBatchReceiveReqDTO reqDTO) {
        // This method can call receiveCoupon for each ID, handling individual errors or batching
        // For simplicity, we'll iterate and collect successes.
        boolean overallSuccess = true;
        for (Long templateId : reqDTO.getCouponIds()) {
            try {
                receiveCoupon(userId, templateId); // Attempt to receive each coupon
            } catch (BusinessException e) {
                log.warn("Failed to receive coupon {} for user {}: {}", templateId, userId, e.getMessage());
                overallSuccess = false; // Mark overall operation as partially failed
            }
        }
        // Return a generic success/failure for batch, or a list of individual results
        return new CouponReceiveResDTO(overallSuccess, null); // couponId is null for batch success
    }

    private CouponListResDTO.CouponDTO mapToCouponDTO(UserCouponEntity entity) {
        CouponListResDTO.CouponDTO dto = new CouponListResDTO.CouponDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getCouponTemplate().getName());
        dto.setType(entity.getCouponTemplate().getType());
        dto.setValue(entity.getCouponTemplate().getValue());
        dto.setMinAmount(entity.getCouponTemplate().getMinAmount());
        dto.setDescription(entity.getCouponTemplate().getDescription());
        dto.setUsage(entity.getCouponTemplate().getUsageScope());
        // For targetIds, parse JSONB string to List<Long>
        if (entity.getCouponTemplate().getTargetIds() != null) {
            try {
                // 使用 TypeReference 来指定泛型类型，避免未经检查的转换警告
                dto.setTargetIds(objectMapper.readValue(
                        entity.getCouponTemplate().getTargetIds(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {}
                ));
            } catch (JsonProcessingException e) {
                log.error("Error parsing targetIds JSON: {}", entity.getCouponTemplate().getTargetIds(), e);
                dto.setTargetIds(List.of());
            }
        } else {
            dto.setTargetIds(List.of());
        }
        dto.setStatus(entity.getStatus());
        dto.setReceivedAt(entity.getReceivedAt());
        dto.setExpireAt(entity.getExpireAt());
        dto.setUsedAt(entity.getUsedAt());
        dto.setOrderId(entity.getOrderId());

        // Computed fields
        boolean canUse = "available".equalsIgnoreCase(entity.getStatus()) && entity.getExpireAt().isAfter(Instant.now());
        dto.setCanUse(canUse);
        if (!canUse) {
            if ("used".equalsIgnoreCase(entity.getStatus())) {
                dto.setUnusableReason("已使用");
            } else if (entity.getExpireAt().isBefore(Instant.now())) {
                dto.setUnusableReason("已过期");
            } else {
                dto.setUnusableReason("不可用");
            }
        }

        return dto;
    }
}