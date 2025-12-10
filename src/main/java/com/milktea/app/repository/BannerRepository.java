// File: milktea-backend/src/main/java/com.milktea.app/repository/BannerRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.BannerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<BannerEntity, Long> {
    List<BannerEntity> findByIsActiveTrueOrderBySortOrderAsc();
}