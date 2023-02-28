package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Book;

import java.util.List;

public interface BooksByTitle {
    List<Book> booksByTitle(String title);
}
