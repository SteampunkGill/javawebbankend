// File: milktea-backend/src/main/java/com.milktea.app/repository/UserSearchHistoryRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.UserSearchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSearchHistoryRepository extends JpaRepository<UserSearchHistoryEntity, Long> {
    List<UserSearchHistoryEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByUserId(Long userId);
}