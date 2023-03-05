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

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
class H2AuthorRepositoryTest {
    private static final String NAME = "Author, Name";
    private static final SiteType TYPE = SiteType.HOMEPAGE;
    private static final URL SITE_URL;

    static {
        try {
            SITE_URL = new URL("https://nu.nl");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

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
    void findAuthorById() {
        final H2BookRepository repository = new H2BookRepository(jdbcTemplate);
        assertNull(repository.findAuthorById(AuthorId.withoutId()));

        final AuthorId authorId = repository.findAuthors().get(0).id();
        assertEquals(authorId, repository.findAuthorById(authorId).id());
    }

    @Test
    void registerAuthor() {
        final H2BookRepository repository = new H2BookRepository(jdbcTemplate);
        final int count = repository.findAuthors().size();

        final Author author = repository.registerAuthor(NAME, Map.of(TYPE, SITE_URL));

        assertNotNull(author);
        assertNotNull(author.id());
        assertEquals(NAME, author.name());
        assertEquals(1, author.sites().size());
        assertEquals(SITE_URL, author.sites().get(SiteType.HOMEPAGE));
        assertEquals(count + 1, repository.findAuthors().size());
        assertEquals(NAME, repository.findAuthorById(author.id()).name());
    }

    @Test
    void updateAuthor() {
        final H2BookRepository repository = new H2BookRepository(jdbcTemplate);

        final String oldName = "Old, Name";
        final String newName = "New, Name";

        Author author = repository.registerAuthor(oldName, Map.of(TYPE, SITE_URL));
        repository.updateAuthor(author.id(), newName);

        author = repository.findAuthorById(author.id());
        assertEquals(newName, author.name());
        assertEquals(author.sites(), Map.of(TYPE, SITE_URL));
    }

    @Test
    void forgetAuthor() {
        final H2BookRepository repository = new H2BookRepository(jdbcTemplate);

        final String oldName = "Old, Name";

        Author author = repository.registerAuthor(oldName, Map.of(TYPE, SITE_URL));
        author = repository.findAuthorById(author.id());
        assertNotNull(author);
        repository.forgetAuthor(author.id());
        author = repository.findAuthorById(author.id());
        assertNull(author);
    }

    @Test
    void setAuthorSite() {
        final H2BookRepository repository = new H2BookRepository(jdbcTemplate);
        Author author = repository.registerAuthor("Old,Name", Map.of());

        assertEquals(0, author.sites().size());
        author = repository.setAuthorSite(author.id(), TYPE, SITE_URL);
        assertEquals(1, author.sites().size());
        assertEquals(SITE_URL, author.sites().get(TYPE));
    }
}