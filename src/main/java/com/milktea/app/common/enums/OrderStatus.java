// File: milktea-backend/src/main/java/com/milktea/app/common/enums/OrderStatus.java
package com.milktea.app.common.enums;

public enum OrderStatus {
    CREATED("created", "订单已创建"),
    PAID("paid", "支付成功"),
    MAKING("making", "制作中"),
    READY("ready", "待取餐/待配送"),
    DELIVERING("delivering", "配送中"), // For delivery type
    COMPLETED("completed", "订单完成"),
    CANCELLED("cancelled", "订单已取消"),
    REFUNDED("refunded", "订单已退款");

    private final String code;
    private final String description;

    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }
}