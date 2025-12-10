// File: milktea-backend/src/main/java/com.milktea.app/repository/OrderRefundImageRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.OrderRefundImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRefundImageRepository extends JpaRepository<OrderRefundImageEntity, Long> {
    // Add custom queries if needed
}