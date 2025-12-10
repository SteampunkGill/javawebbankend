package com.milktea.app.repository;

import com.milktea.app.entity.ProductCustomizationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCustomizationTypeRepository extends JpaRepository<ProductCustomizationTypeEntity, Long> {
    Optional<ProductCustomizationTypeEntity> findByTypeNameAndProductId(String typeName, Long productId);
    List<ProductCustomizationTypeEntity> findByProductIdAndIsEnabledTrueOrderBySortOrderAsc(Long productId);
}