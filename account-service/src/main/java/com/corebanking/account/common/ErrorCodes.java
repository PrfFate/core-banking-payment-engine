package com.corebanking.account.common;

public final class ErrorCodes {
    
    private ErrorCodes() {
        // Prevent instantiation
    }

    // Validation Errors
    public static final String VAL_IDENTITY_REQUIRED = "ERR_VAL_IDENTITY_REQUIRED";
    public static final String VAL_PASSWORD_REQUIRED = "ERR_VAL_PASSWORD_REQUIRED";
    public static final String VAL_PASSWORD_MIN_LENGTH = "ERR_VAL_PASSWORD_MIN_LENGTH";
    public static final String VAL_FULLNAME_REQUIRED = "ERR_VAL_FULLNAME_REQUIRED";
    public static final String VAL_INVALID_TCKN = "ERR_VAL_INVALID_TCKN";
    public static final String VAL_CURRENCY_REQUIRED = "ERR_VAL_CURRENCY_REQUIRED";
    public static final String VAL_FAILED = "ERR_VALIDATION_FAILED";

    // Authentication Errors
    public static final String AUTH_USER_EXISTS = "ERR_AUTH_USER_EXISTS";
    public static final String AUTH_INVALID_CREDENTIALS = "ERR_AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_USER_NOT_FOUND = "ERR_AUTH_USER_NOT_FOUND";

    // Account Errors
    public static final String ACC_NOT_FOUND = "ERR_ACC_NOT_FOUND";
    public static final String ACC_CREATION_FAILED = "ERR_ACC_CREATION_FAILED";

    // System Errors
    public static final String BAD_REQUEST = "ERR_BAD_REQUEST";
    public static final String INTERNAL_SERVER = "ERR_INTERNAL_SERVER";
}
