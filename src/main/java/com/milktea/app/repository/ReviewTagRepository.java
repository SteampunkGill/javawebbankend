package com.milktea.app.repository;

import com.milktea.app.entity.ReviewTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewTagRepository extends JpaRepository<ReviewTagEntity, Long> {
    Optional<ReviewTagEntity> findByName(String name);
}