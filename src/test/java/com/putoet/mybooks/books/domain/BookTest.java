package com.putoet.mybooks.books.domain;

import jakarta.activation.MimeType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class BookTest {
    private static final BookId id = new BookId(BookId.BookIdScheme.ISBN, "978-1-83921-196-6");
    private static final String title = "Get Your Hands Dirty on Clean Architecture";
    private static final Author author = mock(Author.class);
    private static final Set<Author> authors = Set.of(author);
    private static final Set<String> keywords = Set.of("architecture", "adapters", "ports");
    private static final Set<MimeType> formats = Set.of(MimeTypes.EPUB);
    public static final Book book = new Book(id, title, authors, keywords, formats);

    @Test
    void constructor() {
        assertAll(
                // error conditions
                () -> assertThrows(NullPointerException.class, () -> new Book(null,null, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(id, null, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(id, null, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(id, title, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(id, title, authors, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(id, title, authors, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(id, title, authors, keywords, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Book(id, "", authors, keywords, formats)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Book(id, " ", authors, keywords, formats)),


                // Description, formats and keywords may be empty
                () -> new Book(id, title, authors, keywords, formats),
                () -> new Book(id, title, authors, Set.of(), formats),
                () -> new Book(id, title, authors, keywords, Set.of()),

                // correctly constructed book
                () -> new Book(id, title, authors, keywords, formats)
        );
    }

    @Test
    void addFormat() {
        final var updated = book.addFormat(MimeTypes.PDF);

        assertAll(
                () -> assertNotEquals(book, updated),
                () -> assertEquals(2, updated.formats().size()),
                () -> assertTrue(updated.formats().contains(MimeTypes.PDF)),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> updated.addFormat(MimeTypes.PDF))
        );
    }

    @Test
    void addKeyword() {
        final var updated = book.addKeyword(" Hexagonal");

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> book.addKeyword(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> book.addKeyword(" ")),
                () -> assertNotEquals(book, updated),
                () -> assertEquals(4, updated.keywords().size()),
                () -> assertTrue(updated.keywords().contains("hexagonal")),
                () -> assertThrows(IllegalArgumentException.class, () -> updated.addKeyword("hexagonal"))
        );
    }

    @Test
    void addAuthor() {
        final var me = new Author(AuthorId.withoutId(), Instant.now(), "My Name", Map.of());
        final var updated = book.addAuthor(me);

        assertAll(
                () -> assertNotEquals(book, updated),
                () -> assertEquals(2, updated.authors().size()),
                () -> assertTrue(updated.authors().contains(me)),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> updated.addAuthor(me))
        );
    }
}