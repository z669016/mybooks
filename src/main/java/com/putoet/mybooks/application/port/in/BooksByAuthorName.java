package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Book;

import java.util.List;

public interface BooksByAuthorName {
    List<Book> booksByAuthorName(String name);
}
