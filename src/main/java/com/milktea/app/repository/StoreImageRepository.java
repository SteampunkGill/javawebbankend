// File: milktea-backend/src/main/java/com.milktea.app/repository/StoreImageRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.StoreImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreImageRepository extends JpaRepository<StoreImageEntity, Long> {
    List<StoreImageEntity> findByStoreIdOrderBySortOrderAsc(Long storeId);
}