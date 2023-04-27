package com.putoet.mybooks.books.domain.security;

public enum AccessRole {
    ADMIN,
    USER;

    public static AccessRole from(String accessRole) {
        return switch (accessRole.trim().toUpperCase()) {
            case "ADMIN" -> AccessRole.ADMIN;
            case "USER" -> AccessRole.USER;
            default ->
                throw new IllegalArgumentException("Invalid AccessRole " + accessRole);
        };
    }
}
