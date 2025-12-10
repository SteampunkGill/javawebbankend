// File: milktea-backend/src/main/java/com.milktea.app/repository/UserFavoriteProductRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.UserFavoriteProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteProductRepository extends JpaRepository<UserFavoriteProductEntity, Long> {
    List<UserFavoriteProductEntity> findByUserId(Long userId);
    Optional<UserFavoriteProductEntity> findByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserIdAndProductId(Long userId, Long productId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}