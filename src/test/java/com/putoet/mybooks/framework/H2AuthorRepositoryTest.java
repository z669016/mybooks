package com.putoet.mybooks.framework;

import com.putoet.mybooks.domain.Author;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
class H2AuthorRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findAuthorByName() {
        final H2BookRepository repository = new H2BookRepository(jdbcTemplate);
        final List<Author> authors = repository.findAuthorsByName("tom");

        assertEquals(1, authors.size());
        System.out.println(authors.get(0));
    }

    @Test
    void findAuthors() {
        final H2BookRepository repository = new H2BookRepository(jdbcTemplate);
        final List<Author> authors = repository.findAuthors();

        assertEquals(1, authors.size());
        System.out.println(authors.get(0));
    }
}