// File: src/main/java/com/milktea/app/common/exception/BusinessException.java
package com.milktea.app.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}