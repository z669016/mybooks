package com.putoet.mybooks.books.application.security.event;

import org.springframework.lang.NonNull;

public class UserCreatedSecurityEvent extends UserSecurityEvent {
    public static String NAME = "USER_CREATED";

    public UserCreatedSecurityEvent(@NonNull Object source, @NonNull String id) {
        super(source, NAME, id);
    }
}
