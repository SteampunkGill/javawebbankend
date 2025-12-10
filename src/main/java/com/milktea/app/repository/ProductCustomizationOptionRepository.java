// File: milktea-backend/src/main/java/com/milktea/app/repository/ProductCustomizationOptionRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.ProductCustomizationOptionEntity;
import com.milktea.app.entity.ProductCustomizationTypeEntity; // 添加这个导入
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCustomizationOptionRepository extends JpaRepository<ProductCustomizationOptionEntity, Long> {
    Optional<ProductCustomizationOptionEntity> findByCustomizationTypeTypeNameAndValue(String typeName, String value);

    // 添加缺失的方法
    List<ProductCustomizationOptionEntity> findByCustomizationTypeIdOrderBySortOrderAsc(Long customizationTypeId);

    // 可选：添加其他可能需要的方法
    List<ProductCustomizationOptionEntity> findByCustomizationType_IdOrderBySortOrderAsc(Long customizationTypeId);

    List<ProductCustomizationOptionEntity> findByCustomizationType(ProductCustomizationTypeEntity customizationType);
}