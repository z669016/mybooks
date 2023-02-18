package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;

import java.util.Optional;

public interface AuthorById {
    Optional<Author> authorById(AuthorId id);
}
