// File: milktea-backend/src/main/java/com.milktea.app/repository/UserAddressRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {
    List<UserAddressEntity> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    Optional<UserAddressEntity> findByUserIdAndIsDefaultTrue(Long userId);
    List<UserAddressEntity> findByUserId(Long userId);
    long countByUserId(Long userId);
}