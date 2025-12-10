// File: milktea-backend/src/main/java/com.milktea.app/repository/ProductImageRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {
    List<ProductImageEntity> findByProductIdOrderBySortOrderAsc(Long productId);
}