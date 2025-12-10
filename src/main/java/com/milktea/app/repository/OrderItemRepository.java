// File: milktea-backend/src/main/java/com.milktea.app/repository/OrderItemRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    // Add custom queries if needed
}