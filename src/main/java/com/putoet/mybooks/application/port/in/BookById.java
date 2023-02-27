package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Book;
import com.putoet.mybooks.domain.BookId;

import java.util.Optional;

public interface BookById {
    Optional<Book> bookById(BookId id);
}
