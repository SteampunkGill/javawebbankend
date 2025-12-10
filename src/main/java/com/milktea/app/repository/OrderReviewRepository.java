// File: milktea-backend/src/main/java/com.milktea.app/repository/OrderReviewRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.OrderReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderReviewRepository extends JpaRepository<OrderReviewEntity, Long> {
    Optional<OrderReviewEntity> findByOrderId(Long orderId);
}