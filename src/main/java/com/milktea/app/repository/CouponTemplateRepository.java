// File: milktea-backend/src/main/java/com.milktea.app/repository/CouponTemplateRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.CouponTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplateEntity, Long> {
    // Add custom queries if needed
}