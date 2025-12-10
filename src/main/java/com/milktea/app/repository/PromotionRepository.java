// File: milktea-backend/src/main/java/com.milktea.app/repository/PromotionRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {
    List<PromotionEntity> findByIsActiveTrueAndEndTimeAfterOrderByStartTimeAsc(Instant now);
}