// File: milktea-backend/src/main/java/com.milktea.app/repository/CartItemCustomizationRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.CartItemCustomizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemCustomizationRepository extends JpaRepository<CartItemCustomizationEntity, Long> {
    List<CartItemCustomizationEntity> findByCartItemId(Long cartItemId);
}