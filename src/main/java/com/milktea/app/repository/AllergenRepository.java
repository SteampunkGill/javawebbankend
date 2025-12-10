// File: milktea-backend/src/main/java/com/milktea.app/repository/AllergenRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.AllergenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergenRepository extends JpaRepository<AllergenEntity, Long> {
    // Custom query methods can be added here
}