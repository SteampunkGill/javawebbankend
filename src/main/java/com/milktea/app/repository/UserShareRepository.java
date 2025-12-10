// File: milktea-backend/src/main/java/com.milktea.app/repository/UserShareRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.UserShareEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserShareRepository extends JpaRepository<UserShareEntity, Long> {
    List<UserShareEntity> findByUserIdAndType(Long userId, String type);
    long countByUserIdAndType(Long userId, String type);
}