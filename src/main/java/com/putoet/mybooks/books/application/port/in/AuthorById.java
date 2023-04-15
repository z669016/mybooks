package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;

import java.util.Optional;

public interface AuthorById {
    Optional<Author> authorById(AuthorId id);
}
