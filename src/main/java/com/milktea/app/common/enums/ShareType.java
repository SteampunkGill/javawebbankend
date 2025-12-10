// File: milktea-backend/src/main/java/com/milktea/app/common/enums/ShareType.java
package com.milktea.app.common.enums;

public enum ShareType {
    PRODUCT("product"),
    ACTIVITY("activity"),
    INVITE("invite");

    private final String code;

    ShareType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}