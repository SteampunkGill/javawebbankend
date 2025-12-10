// File: milktea-backend/src/main/java/com.milktea.app/repository/SearchKeywordRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.SearchKeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeywordEntity, Long> {
    Optional<SearchKeywordEntity> findByKeyword(String keyword);
    List<SearchKeywordEntity> findByTypeOrderByCountDesc(String type);
    List<SearchKeywordEntity> findTop5ByKeywordStartingWithOrderByCountDesc(String prefix);
}