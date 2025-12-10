// File: milktea-backend/src/main/java/com.milktea.app/repository/VerificationCodeRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.VerificationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity, Long> {
    Optional<VerificationCodeEntity> findTopByPhoneAndTypeAndExpiresAtAfterAndIsUsedFalseOrderBySentAtDesc(
            String phone, String type, Instant now);
}