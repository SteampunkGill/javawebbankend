// File: milktea-backend/src/main/java/com.milktea.app/repository/PaymentRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByOrderId(Long orderId);
    Optional<PaymentEntity> findByTransactionId(String transactionId);
}