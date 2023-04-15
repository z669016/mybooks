package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.Author;

import java.util.List;

public interface AuthorsByName {
    List<Author> authorsByName(String name);
}
