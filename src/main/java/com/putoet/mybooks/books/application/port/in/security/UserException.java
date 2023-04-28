package com.putoet.mybooks.books.application.port.in.security;

public class UserException extends RuntimeException {
    private final UserError userError;

    public UserException(UserError userError) {
        super(userError.name());
        this.userError = userError;
    }

    public UserException(UserError userError, String msg) {
        super(userError.name() + " - " + msg);
        this.userError = userError;
    }

    public UserError getUserError() {
        return userError;
    }
}
