// File: milktea-backend/src/main/java/com.milktea.app/repository/OrderRefundRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.OrderRefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRefundRepository extends JpaRepository<OrderRefundEntity, Long> {
    // Add custom queries if needed
}