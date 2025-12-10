// File: milktea-backend/src/main/java/com.milktea.app/repository/ProductNutritionRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.ProductNutritionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductNutritionRepository extends JpaRepository<ProductNutritionEntity, Long> {
    List<ProductNutritionEntity> findByProductId(Long productId);
}