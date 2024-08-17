package com.putoet.mybooks.books.application.security.event;

import org.springframework.lang.NonNull;

public class UserDeletedSecurityEvent extends UserSecurityEvent {
    public static String NAME = "USER_DELETED";

    public UserDeletedSecurityEvent(@NonNull Object source, @NonNull String id) {
        super(source, NAME, id);
    }
}
