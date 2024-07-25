package com.putoet.mybooks.books.application.port.in.security;

public enum UserError {
    USER_ID_OR_PASSWORD_ERROR,
    USER_REGISTRATION_ERROR,
    USER_ID_INVALID,
    USER_ID_REQUIRED,
    USER_NAME_REQUIRED,
    USER_PASSWORD_REQUIRED,
    USER_ACCESS_ROLE_REQUIRED,
    USER_ACCESS_ROLE_INVALID,
    USER_PASSWORD_TOO_SIMPLE;

    public RuntimeException exception() {
        return new UserException(this);
    }
    public RuntimeException exception(String msg) {
        return new UserException(this, msg);
    }
}
