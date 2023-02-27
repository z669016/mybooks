package com.putoet.mybooks.application.port.out;

import com.putoet.mybooks.domain.Author;

public interface BookRepository extends BookReadOnlyRepository {
    Author persist(Author author);
}
