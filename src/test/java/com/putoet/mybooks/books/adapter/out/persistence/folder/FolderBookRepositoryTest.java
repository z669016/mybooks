package com.putoet.mybooks.books.adapter.out.persistence.folder;

import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.BookId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FolderBookRepositoryTest {
    private static final String LEANPUB = "/Users/renevanputten/OneDrive/Books/leanpub";
    private static FolderBookRepository leanpub;

    @BeforeAll
    static void bookFolder() {
        leanpub = new FolderBookRepository(Paths.get(LEANPUB));
    }

    @Test
    void findAuthorByName() {
        final var authors = leanpub.findAuthorsByName("stuart");
        assertAll(
                () -> assertEquals(1, authors.size()),
                () -> assertEquals("Gunter, Stuart", authors.stream().findFirst().orElseThrow().name())
        );
    }

    @Test
    void findAuthors() {
        final var authors = leanpub.findAuthors();
        assertEquals(5, authors.size());

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
        final var author = leanpub.findAuthorById(AuthorId.withoutId());
        assertNull(author);
    }

    @Test
    void findBooks() {
        final var books = leanpub.findBooks();
        assertEquals(6, books.size());

        var b = books.stream().findFirst().orElseThrow();
        for (var book : books) {
            if (b.id().equals(book.id()))
                continue;

            if (b.title().compareTo(book.title()) > 0) {
                fail("Books are not ordered");
            }
        }
    }

    @Test
    void findBooksByTitle() {
        final var books = leanpub.findBooksByTitle("ARCHITECTURE");
        assertEquals(3, books.size());
    }

    @Test
    void findBookById() {
        final var id = "https://leanpub.com/wardley-maps";
        final var bookId = new BookId(BookId.BookIdSchema.URL, id);
        final var book = leanpub.findBookById(bookId);

        assertAll(
                () -> assertNotNull(book),
                () -> assertEquals(bookId, book.id())
        );
    }

    @Test
    void findBooksByAuthorId() {
        final var author = leanpub.findAuthorsByName("stuart").stream().findFirst().orElseThrow();
        final var books = leanpub.findBooksByAuthorId(author.id());
        assertEquals(1, books.size());
    }
}