// File: milktea-backend/src/main/java/com.milktea.app/repository/PageConfigRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.PageConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageConfigRepository extends JpaRepository<PageConfigEntity, Long> {
    List<PageConfigEntity> findByPageName(String pageName);
    Optional<PageConfigEntity> findByPageNameAndKey(String pageName, String key);
}