// File: milktea-backend/src/main/java/com/milktea/app/common/enums/PayStatus.java
package com.milktea.app.common.enums;

public enum PayStatus {
    UNPAID("unpaid", "未支付"),
    PAID("paid", "已支付"),
    FAILED("failed", "支付失败"),
    CANCELLED("cancelled", "支付已取消");

    private final String code;
    private final String description;

    PayStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}