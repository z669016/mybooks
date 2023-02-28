package com.putoet.mybooks.framework;

import com.putoet.mybooks.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    void createAuthor() throws MalformedURLException {
        Author author = new Author(AuthorId.withoutId()
                , "Putten, Margot van"
                , Map.of(SiteType.LINKEDIN, new Site(SiteId.withoutId(), SiteType.LINKEDIN,
                    new URL("https://nl.linkedin.com/in/margot-van-putten-3a115615"))
                )
        );

        final H2BookRepository repository = new H2BookRepository(jdbcTemplate);
        author = repository.createAuthor(author);

        assertNotNull(author);

        final List<Author> authors = repository.findAuthors();
        assertEquals(2, authors.size());
    }
}