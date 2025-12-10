// File: milktea-backend/src/main/java/com.milktea.app/repository/CartItemRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findByUserId(Long userId);
    List<CartItemEntity> findByUserIdAndIsSelectedTrue(Long userId);
    Optional<CartItemEntity> findByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserId(Long userId);
}