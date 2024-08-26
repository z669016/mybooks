package com.putoet.mybooks.books.application.security.event;

import com.putoet.mybooks.books.domain.BookId;
import org.springframework.lang.NonNull;

public class BookCreatedSecurityEvent extends UserSecurityEvent {
    public static final String NAME = "BOOK_CREATED";

    public BookCreatedSecurityEvent(@NonNull Object source, @NonNull BookId bookId) {
        super(source, NAME, bookId.toString());
    }
}
