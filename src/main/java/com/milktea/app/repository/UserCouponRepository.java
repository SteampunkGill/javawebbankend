package com.milktea.app.repository;

import com.milktea.app.entity.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCouponEntity, Long> {
    List<UserCouponEntity> findByUserIdAndStatusOrderByExpireAtAsc(Long userId, String status);
    Optional<UserCouponEntity> findByUserIdAndCouponTemplateIdAndStatus(Long userId, Long couponTemplateId, String status);
    long countByUserIdAndStatus(Long userId, String status);
    List<UserCouponEntity> findByUserIdAndExpireAtBeforeAndStatus(Long userId, Instant now, String status);

    // 新增方法
    long countByUserIdAndCouponTemplateIdAndStatus(Long userId, Long couponTemplateId, String status);
}