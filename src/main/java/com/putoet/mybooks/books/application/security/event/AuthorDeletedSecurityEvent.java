package com.putoet.mybooks.books.application.security.event;

import com.putoet.mybooks.books.domain.AuthorId;
import org.springframework.lang.NonNull;

public class AuthorDeletedSecurityEvent extends UserSecurityEvent {
    public static String NAME = "AUTHOR_DELETED";

    public AuthorDeletedSecurityEvent(@NonNull Object source, @NonNull AuthorId authorId) {
        super(source, NAME, authorId.toString());
    }
}
