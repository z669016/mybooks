package com.putoet.mybooks.books.adapter.out.persistence.jdbc;

import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.BookId;
import com.putoet.mybooks.books.domain.MimeTypes;
import com.putoet.mybooks.books.domain.SiteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
class H2BookRepositoryTest {
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

    private H2BookRepository repository;

    @BeforeEach
    void setup() {
        repository = new H2BookRepository(jdbcTemplate);
    }

    @Test
    void findAuthorByName() {
        final var authors = repository.findAuthorsByName("tom");

        assertEquals(1, authors.size());
        System.out.println(authors.stream().findFirst().orElseThrow());
    }

    @Test
    void findAuthors() {
        final var authors = repository.findAuthors();

        assertEquals(6, authors.size());

        var a = authors.stream().findFirst().orElseThrow();
        for (var author : authors) {
            if (a.id().equals(author.id()))
                continue;

            if (a.name().compareTo(author.name()) > 0) {
                fail("Authors are not ordered");
            }
        }
    }

    @Test
    void findAuthorById() {
        assertNull(repository.findAuthorById(AuthorId.withoutId()));

        final var authorId = repository.findAuthors().stream().findFirst().orElseThrow().id();
        assertEquals(authorId, repository.findAuthorById(authorId).id());
    }

    @Test
    void registerAuthor() {
        final int count = repository.findAuthors().size();
        final var author = repository.registerAuthor(NAME, Map.of(TYPE, SITE_URL));

        assertAll(
                () -> assertNotNull(author),
                () -> assertNotNull(author.id()),
                () -> assertEquals(NAME, author.name()),
                () -> assertEquals(1, author.sites().size()),
                () -> assertEquals(SITE_URL, author.sites().get(SiteType.HOMEPAGE)),
                () -> assertEquals(count + 1, repository.findAuthors().size()),
                () -> assertEquals(NAME, repository.findAuthorById(author.id()).name())
        );
    }

    @Test
    void updateAuthor() {
        final var oldName = "Old, Name";
        final var newName = "New, Name";

        final var original = repository.registerAuthor(oldName, Map.of(TYPE, SITE_URL));
        final var author = repository.updateAuthor(original.id(), original.version(), newName);

        assertAll(
                () -> assertNotEquals(original.version(), author.version()),
                () -> assertEquals(newName, author.name()),
                () -> assertEquals(author.sites(), Map.of(TYPE, SITE_URL))
        );
    }

    @Test
    void updateAuthorInvalidVersion() {
        final var author = repository.registerAuthor("oldName", Map.of(TYPE, SITE_URL));
        assertThrows(ServiceException.class, () -> repository.updateAuthor(author.id(), Instant.now(), "newName"));
    }

    @Test
    void forgetAuthor() {
        final var oldName = "Old, Name";
        final var author = repository.registerAuthor(oldName, Map.of(TYPE, SITE_URL));
        repository.forgetAuthor(author.id());

        assertNull(repository.findAuthorById(author.id()));
    }

    @Test
    void setAuthorSite() {
        var author = repository.registerAuthor("Old,Name", Map.of());

        assertEquals(0, author.sites().size());
        author = repository.setAuthorSite(author.id(), TYPE, SITE_URL);
        assertEquals(1, author.sites().size());
        assertEquals(SITE_URL, author.sites().get(TYPE));
    }


    @Test
    void registerBook() {
        final var author = repository.findAuthorsByName("tom").stream().findFirst().orElseThrow();
        final var bookId = new BookId(BookId.BookIdSchema.ISBN, "978-1839211966");
        final var title = "Get Your Hands Dirty on Clean Architecture";
        final var authors = Set.of(author);
        final var formats = Set.of(MimeTypes.EPUB);
        final var keywords = Set.of("A", "B");

        final var book = repository.registerBook(bookId, title, authors, formats, keywords);
        assertAll(
                () -> assertNotNull(book),
                () -> assertEquals(bookId, book.id()),
                () -> assertEquals(title, book.title()),
                () -> assertEquals(authors, book.authors()),
                () -> assertEquals(formats, book.formats()),
                () -> assertEquals(keywords, book.keywords())
        );
    }
}