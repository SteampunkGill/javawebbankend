// File: milktea-backend/src/main/java/com/milktea/app/common/constant/ErrorCode.java
package com.milktea.app.common.constant;

public final class ErrorCode {
    // General Errors
    public static final int SUCCESS = 0;
    public static final int SYSTEM_ERROR = 10000;
    public static final int INVALID_PARAM = 10001;
    public static final int UNAUTHORIZED = 10002;
    public static final int FORBIDDEN = 10003;
    public static final int NOT_FOUND = 10004;
    public static final int CONFLICT = 10005; // Resource conflict (e.g., duplicate entry)

    // Auth Errors
    public static final int WECHAT_LOGIN_FAILED = 20001;
    public static final int ACCOUNT_LOGIN_FAILED = 20002;
    public static final int INVALID_CREDENTIALS = 20003;
    public static final int USER_NOT_EXIST = 20004;
    public static final int USER_ALREADY_EXISTS = 20005;
    public static final int INVALID_VERIFICATION_CODE = 20006;
    public static final int VERIFICATION_CODE_EXPIRED = 20007;

    // User Profile Errors
    public static final int PHONE_ALREADY_BOUND = 30001;
    public static final int ADDRESS_NOT_FOUND = 30002;
    public static final int DEFAULT_ADDRESS_CANNOT_DELETE = 30003;

    // Product Errors
    public static final int PRODUCT_NOT_FOUND = 40001;
    public static final int PRODUCT_OFFLINE = 40002;
    public static final int PRODUCT_STOCK_INSUFFICIENT = 40003;

    // Cart Errors
    public static final int CART_ITEM_NOT_FOUND = 50001;
    public static final int CART_ITEM_INVALID = 50002;
    public static final int CART_EMPTY = 50003;

    // Order Errors
    public static final int ORDER_NOT_FOUND = 60001;
    public static final int ORDER_STATUS_INVALID = 60002;
    public static final int ORDER_CANNOT_CANCEL = 60003;
    public static final int ORDER_CANNOT_REFUND = 60004;
    public static final int PAYMENT_FAILED = 60005;
    public static final int PAYMENT_EXPIRED = 60006;
    public static final int PAYMENT_METHOD_NOT_SUPPORTED = 60007;
    public static final int CHECKOUT_VALIDATION_FAILED = 60008;

    // Store Errors
    public static final int STORE_NOT_FOUND = 70001;
    public static final int STORE_CLOSED = 70002;
    public static final int STORE_BUSY = 70003;
    public static final int STORE_OUT_OF_DELIVERY_RANGE = 70004;
    public static final int MINIMUM_ORDER_AMOUNT_NOT_MET = 70005;

    // Coupon Errors
    public static final int COUPON_NOT_FOUND = 80001;
    public static final int COUPON_EXPIRED = 80002;
    public static final int COUPON_USED = 80003;
    public static final int COUPON_NOT_APPLICABLE = 80004;
    public static final int COUPON_ACQUIRE_LIMIT_REACHED = 80005;
    public static final int COUPON_OUT_OF_STOCK = 80006;

    // Point Errors
    public static final int INSUFFICIENT_POINTS = 90001;
    public static final int POINT_EXCHANGE_ITEM_NOT_FOUND = 90002;
    public static final int POINT_EXCHANGE_ITEM_OUT_OF_STOCK = 90003;

    // File Upload Errors
    public static final int FILE_UPLOAD_FAILED = 11001;
    public static final int INVALID_FILE_TYPE = 11002;
    public static final int FILE_SIZE_EXCEEDS_LIMIT = 11003;

    private ErrorCode() {
        // Private constructor to prevent instantiation
    }
}