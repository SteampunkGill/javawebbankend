// File: milktea-backend/src/main/java/com.milktea.app/repository/MemberLevelRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.MemberLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberLevelRepository extends JpaRepository<MemberLevelEntity, Long> {
    Optional<MemberLevelEntity> findFirstByMinGrowthValueLessThanEqualOrderByMinGrowthValueDesc(Integer growthValue);
    Optional<MemberLevelEntity> findFirstByMinGrowthValueGreaterThanOrderByMinGrowthValueAsc(Integer growthValue);
    Optional<MemberLevelEntity> findByName(String name);
}