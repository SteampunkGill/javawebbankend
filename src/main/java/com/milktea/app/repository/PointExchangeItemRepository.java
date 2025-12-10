// File: milktea-backend/src/main/java/com.milktea.app/repository/PointExchangeItemRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.PointExchangeItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointExchangeItemRepository extends JpaRepository<PointExchangeItemEntity, Long> {
    // Add custom queries if needed
}