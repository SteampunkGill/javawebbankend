// File: milktea-backend/src/main/java/com.milktea.app/repository/SystemConfigRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemConfigRepository extends JpaRepository<SystemConfigEntity, String> {
    Optional<SystemConfigEntity> findByKey(String key);
}