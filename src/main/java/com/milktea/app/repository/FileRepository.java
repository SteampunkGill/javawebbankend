// File: milktea-backend/src/main/java/com.milktea.app/repository/FileRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    // Custom query methods can be added here
}