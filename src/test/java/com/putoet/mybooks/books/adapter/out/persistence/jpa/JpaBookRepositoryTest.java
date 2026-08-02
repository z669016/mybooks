package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@DataJpaTest
class JpaBookRepositoryTest {
    @Autowired
    private AuthorJpaRepository authorRepository;

    @Autowired
    private BookJpaRepository bookRepository;

    private final DomainMapper mapper = new DomainMapper();

    private BookPersistenceUpdatePort repository;

    @SneakyThrows
    @BeforeEach
    void setup() {
        repository = new JpaBookRepository(mapper, authorRepository, bookRepository);
    }

    @Test
    void test1() {
        final var authors = repository.findAuthors();
        assertNotNull(authors);
    }

    @Test
    void test2() {
        final var books = repository.findBooks();
        assertNotNull(books);
    }
}
