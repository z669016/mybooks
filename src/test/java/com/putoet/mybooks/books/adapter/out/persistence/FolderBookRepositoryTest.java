package com.putoet.mybooks.books.adapter.out.persistence;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.Book;
import com.putoet.mybooks.books.domain.BookId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FolderBookRepositoryTest {
    private static final String LEANPUB = "/Users/renevanputten/OneDrive/Documents/Books/leanpub";
    private static FolderBookRepository leanpub;

    @BeforeAll
    static void bookFolder() {
        leanpub = new FolderBookRepository(Paths.get(LEANPUB));
    }

    @Test
    void findAuthorByName() {
        final List<Author> authors = leanpub.findAuthorsByName("stuart");
        assertAll(
                () -> assertEquals(1, authors.size()),
                () -> assertEquals("Gunter, Stuart", authors.get(0).name())
        );
    }

    @Test
    void findAuthors() {
        final List<Author> authors = leanpub.findAuthors();
        assertEquals(6, authors.size());
    }

    @Test
    void findAuthorById() {
        final Author author = leanpub.findAuthorById(AuthorId.withoutId());
        assertNull(author);
    }

    @Test
    void findBooks() {
        final List<Book> books = leanpub.findBooks();
        assertEquals(6, books.size());
    }

    @Test
    void findBooksByTitle() {
        final List<Book> books = leanpub.findBooksByTitle("ARCHITECTURE");
        assertEquals(2, books.size());
    }

    @Test
    void findBookById() {
        final String id = "https://leanpub.com/wardley-maps";
        final BookId bookId = new BookId(BookId.BookIdScheme.URL, id);
        final Book book = leanpub.findBookById(bookId);

        assertAll(
                () -> assertNotNull(book),
                () -> assertEquals(bookId, book.id())
        );
    }

    @Test
    void findBooksByAuthorId() {
        final Author author = leanpub.findAuthorsByName("stuart").get(0);
        final List<Book> books = leanpub.findBooksByAuthorId(author.id());
        assertEquals(1, books.size());
    }
}