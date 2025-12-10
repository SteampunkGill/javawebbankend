// File: milktea-backend/src/main/java/com.milktea.app/repository/StoreRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    List<StoreEntity> findByIsActiveTrue();

    // Placeholder for distance-based search. Actual implementation would need PostGIS or similar.
    // This query just returns active stores for demonstration.
    @Query(value = "SELECT s.*, " +
            "ACOS(SIN(RADIANS(:latitude)) * SIN(RADIANS(s.latitude)) + COS(RADIANS(:latitude)) * COS(RADIANS(s.latitude)) * COS(RADIANS(s.longitude) - RADIANS(:longitude))) * 6371000 AS distance_meters " +
            "FROM stores s " +
            "WHERE s.is_active = TRUE " +
            "HAVING ACOS(SIN(RADIANS(:latitude)) * SIN(RADIANS(s.latitude)) + COS(RADIANS(:latitude)) * COS(RADIANS(s.latitude)) * COS(RADIANS(s.longitude) - RADIANS(:longitude))) * 6371000 <= :radius " +
            "ORDER BY distance_meters ASC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<StoreEntity> findNearbyStoresNative(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") Double radius, // in meters
            @Param("limit") Integer limit
    );
}