package com.putoet.mybooks.books.adapter.out.persistence;

import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private H2BookRepository repository;

    @BeforeEach
    void setup() {
        repository = new H2BookRepository(jdbcTemplate);
    }

    @Test
    void findAuthorByName() {
        final List<Author> authors = repository.findAuthorsByName("tom");

        assertEquals(1, authors.size());
        System.out.println(authors.get(0));
    }

    @Test
    void findAuthors() {
        final List<Author> authors = repository.findAuthors();

        assertEquals(6, authors.size());
        System.out.println(authors.get(0));
    }

    @Test
    void findAuthorById() {
        assertNull(repository.findAuthorById(AuthorId.withoutId()));

        final AuthorId authorId = repository.findAuthors().get(0).id();
        assertEquals(authorId, repository.findAuthorById(authorId).id());
    }

    @Test
    void registerAuthor() {
        final int count = repository.findAuthors().size();
        final Author author = repository.registerAuthor(NAME, Map.of(TYPE, SITE_URL));

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
        final String oldName = "Old, Name";
        final String newName = "New, Name";

        final Author original = repository.registerAuthor(oldName, Map.of(TYPE, SITE_URL));
        final Author author = repository.updateAuthor(original.id(), original.version(), newName);

        assertAll(
                () -> assertNotEquals(original.version(), author.version()),
                () -> assertEquals(newName, author.name()),
                () -> assertEquals(author.sites(), Map.of(TYPE, SITE_URL))
        );
    }

    @Test
    void updateAuthorInvalidVersion() {
        Author author = repository.registerAuthor("oldName", Map.of(TYPE, SITE_URL));
        assertThrows(ServiceException.class, () -> repository.updateAuthor(author.id(), Instant.now(), "newName"));
    }

    @Test
    void forgetAuthor() {
        final String oldName = "Old, Name";
        final Author author = repository.registerAuthor(oldName, Map.of(TYPE, SITE_URL));
        repository.forgetAuthor(author.id());

        assertNull(repository.findAuthorById(author.id()));
    }

    @Test
    void setAuthorSite() {
        Author author = repository.registerAuthor("Old,Name", Map.of());

        assertEquals(0, author.sites().size());
        author = repository.setAuthorSite(author.id(), TYPE, SITE_URL);
        assertEquals(1, author.sites().size());
        assertEquals(SITE_URL, author.sites().get(TYPE));
    }


    @Test
    void registerBook() {
        final Author author = repository.findAuthorsByName("tom").get(0);
        final BookId bookId = new BookId(BookId.BookIdScheme.ISBN, "978-1839211966");
        final String title = "Get Your Hands Dirty on Clean Architecture";
        final List<Author> authors = List.of(author);
        final MimeTypes formats = new MimeTypes(List.of(MimeTypes.EPUB));
        final Set<String> keywords = Set.of("A", "B");

        final Book book = repository.registerBook(bookId, title, authors, formats, keywords);
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