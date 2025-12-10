// File: milktea-backend/src/main/java/com.milktea.app/entity/UserFavoriteStoreEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "user_favorite_stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserFavoriteStoreEntity.UserFavoriteStoreId.class)
public class UserFavoriteStoreEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserFavoriteStoreId implements Serializable {
        private Long user;
        private Long store;
    }
}