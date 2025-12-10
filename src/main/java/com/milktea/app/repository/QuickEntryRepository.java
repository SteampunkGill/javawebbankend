// File: milktea-backend/src/main/java/com.milktea.app/repository/QuickEntryRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.QuickEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuickEntryRepository extends JpaRepository<QuickEntryEntity, Long> {
    List<QuickEntryEntity> findByIsActiveTrueOrderBySortOrderAsc();
}