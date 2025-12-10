// File: milktea-backend/src/main/java/com.milktea.app/entity/UserShareEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "user_shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserShareEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // product, activity, invite

    @Column(name = "target_id", nullable = false, length = 50)
    private String targetId;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel; // wechat, moments, qq

    @Column(name = "invite_code", length = 50)
    private String inviteCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}