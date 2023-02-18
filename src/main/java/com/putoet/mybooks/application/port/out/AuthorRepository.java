package com.putoet.mybooks.application.port.out;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;

import java.util.List;

public interface AuthorRepository {
    List<Author> findAuthorByName(String name);
    Author findAuthorById(AuthorId id);
    Author persist(Author author);
}
