// File: milktea-backend/src/main/java/com.milktea.app/repository/CategoryRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();
    List<CategoryEntity> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);
    List<CategoryEntity> findByIsActiveTrueOrderBySortOrderAsc();
}