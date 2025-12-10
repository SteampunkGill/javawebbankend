// File: milktea-backend/src/main/java/com.milktea.app/dto/user/ShareInfoResDTO.java
package com.milktea.app.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareInfoResDTO {
    private String title;
    private String description;
    private String image;
    private String path;
    private String inviteCode;
    private Integer inviteCount;
    private Integer rewardPoints;
    private RewardCouponDTO rewardCoupon;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardCouponDTO {
        private String id;
        private String name;
        private BigDecimal amount;
    }
}