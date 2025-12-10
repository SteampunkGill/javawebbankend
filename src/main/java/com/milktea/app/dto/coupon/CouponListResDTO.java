package com.milktea.app.dto.coupon;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CouponListResDTO {
    private List<CouponDTO> coupons;
    private int total;
    private int page;
    private int limit;

    @Data
    public static class CouponDTO {
        private Long id;
        private String name;
        private String type;
        private BigDecimal value;
        private BigDecimal minAmount;
        private String description;
        private String usage;
        private List<Long> targetIds;
        private String status;
        private Instant receivedAt;
        private Instant expireAt;
        private Instant usedAt;
        private Long orderId;
        private Boolean canUse;
        private String unusableReason;

        // 确保有 getter 和 setter
        public String getUnusableReason() {
            return unusableReason;
        }

        public void setUnusableReason(String unusableReason) {
            this.unusableReason = unusableReason;
        }
    }
}