package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.Book;

import java.util.List;

public interface BooksByTitle {
    List<Book> booksByTitle(String title);
}
