// File: milktea-backend/src/main/java/com.milktea.app/repository/UserFavoriteStoreRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.UserFavoriteStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavoriteStoreRepository extends JpaRepository<UserFavoriteStoreEntity, Long> {
    boolean existsByUserIdAndStoreId(Long userId, Long storeId);

    @Modifying
    @Query("DELETE FROM UserFavoriteStoreEntity ufs WHERE ufs.user.id = :userId AND ufs.store.id = :storeId")
    void deleteByUserIdAndStoreId(@Param("userId") Long userId, @Param("storeId") Long storeId);
}