package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.Book;
import com.putoet.mybooks.books.domain.BookId;

import java.util.Optional;

public interface BookById {
    Optional<Book> bookById(BookId id);
}
