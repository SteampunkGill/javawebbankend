// File: milktea-backend/src/main/java/com.milktea.app/repository/IngredientRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {
    // Custom query methods can be added here
}