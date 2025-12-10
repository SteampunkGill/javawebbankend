// File: milktea-backend/src/main/java/com.milktea.app/dto/user/UserProfileResDTO.java
package com.milktea.app.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResDTO {
    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender; // 1:男,2:女, 0:未知
    private LocalDate birthday;
    private String level; // gold
    private String levelName; // 黄金会员
    private Integer points;
    private BigDecimal balance;
    private Integer growthValue;
    private Integer nextLevelPoints;
    private Integer couponCount;
    private Integer unreadMessageCount;
    private MemberCardDTO memberCard;
    private Instant createdAt;

    // Separate DTO for the nested 'user' object in UserAuthResDTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetailDTO {
        private String id; // Changed to String as per JSON spec, although Long in DB
        private String nickname;
        private String avatar;
        private String phone;
        private String level; // 0:普通, 1:黄金,2:钻石 (JSON spec says "gold" but then 0,1,2. I'll stick to string for DTO and map logic.)
        private Integer points;
        private BigDecimal balance;
        private LocalDate birthday;
        private Instant createdAt;
        private String levelName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberCardDTO {
        private String cardNo;
        private String status;
        private LocalDate expireDate;
    }
}