package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;

import java.time.Instant;

public interface UpdateAuthor {
    Author updateAuthor(AuthorId authorId, Instant version, String name);
}
