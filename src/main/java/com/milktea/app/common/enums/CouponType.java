// File: milktea-backend/src/main/java/com/milktea/app/common/enums/CouponType.java
package com.milktea.app.common.enums;

public enum CouponType {
    DISCOUNT("discount"), // 满减
    PERCENTAGE("percentage"), // 折扣
    FIXED("fixed"); // 固定金额

    private final String code;

    CouponType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}