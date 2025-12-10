// File: milktea-backend/src/main/java/com.milktea.app/repository/OrderReviewImageRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.OrderReviewImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReviewImageRepository extends JpaRepository<OrderReviewImageEntity, Long> {
    // Add custom queries if needed
}