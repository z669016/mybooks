package com.putoet.mybooks.books.application.security.event;

import com.putoet.mybooks.books.domain.AuthorId;
import org.springframework.lang.NonNull;

public class AuthorCreatedSecurityEvent extends UserSecurityEvent {
    public static final String NAME = "AUTHOR_CREATED";

    public AuthorCreatedSecurityEvent(@NonNull Object source, @NonNull AuthorId authorId) {
        super(source, NAME, authorId.toString());
    }
}
