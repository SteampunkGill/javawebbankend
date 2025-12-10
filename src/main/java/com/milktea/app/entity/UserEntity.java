// File: milktea-backend/src/main/java/com.milktea.app/entity/UserEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wechat_openid", unique = true, length = 64)
    private String wechatOpenid;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @JsonIgnore // Don't expose password hash in API responses
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "gender")
    private Short gender; // 0:unknown, 1:male, 2:female

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "birthday")
    private LocalDate birthday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_level_id")
    private MemberLevelEntity memberLevel;

    @Column(name = "growth_value", nullable = false)
    private Integer growthValue;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "member_card_no", unique = true, length = 50)
    private String memberCardNo;

    @Column(name = "member_card_status", length = 20)
    private String memberCardStatus; // active, expired, inactive

    @Column(name = "member_card_expire_date")
    private LocalDate memberCardExpireDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
        if (gender == null) {
            gender = 0; // Default to unknown
        }
        if (growthValue == null) {
            growthValue = 0;
        }
        if (points == null) {
            points = 0;
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (memberCardStatus == null) {
            memberCardStatus = "inactive";
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}