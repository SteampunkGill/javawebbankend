// File: milktea-backend/src/main/java/com.milktea.app/repository/OrderItemCustomizationRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.OrderItemCustomizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemCustomizationRepository extends JpaRepository<OrderItemCustomizationEntity, Long> {
    // Add custom queries if needed
}