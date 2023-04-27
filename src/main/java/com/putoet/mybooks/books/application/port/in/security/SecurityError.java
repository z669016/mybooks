package com.putoet.mybooks.books.application.port.in.security;

public enum SecurityError {
    USER_ID_OR_PASSWORD_ERROR,
    USER_REGISTRATION_ERROR,
    USER_ID_INVALID,
    USER_ID_REQUIRED,
    USER_NAME_REQUIRED,
    USER_PASSWORD_REQUIRED,
    USER_ACCESS_ROLE_REQUIRED,
    USER_ACCESS_ROLE_INVALID,
    USER_PASSWORD_TOO_SIMPLE;

    public void raise() {
        throw new SecurityException(this);
    }
    public void raise(String msg) {
        throw new SecurityException(this, msg);
    }
}
