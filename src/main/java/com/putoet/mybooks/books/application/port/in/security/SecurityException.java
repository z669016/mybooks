package com.putoet.mybooks.books.application.port.in.security;

public class SecurityException extends RuntimeException {
    private final SecurityError securityError;

    public SecurityException(SecurityError securityError) {
        super(securityError.name());
        this.securityError = securityError;
    }

    public SecurityException(SecurityError securityError, String msg) {
        super(securityError.name() + " - " + msg);
        this.securityError = securityError;
    }

    public SecurityError getSecurityError() {
        return securityError;
    }
}
