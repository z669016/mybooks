package com.putoet.mybooks.books.application.security.event;

import org.jspecify.annotations.NonNull;

public class UserCreatedSecurityEvent extends UserSecurityEvent {
    public static final String NAME = "USER_CREATED";

    public UserCreatedSecurityEvent(@NonNull Object source, @NonNull String id) {
        super(source, NAME, id);
    }
}
