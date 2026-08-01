package com.putoet.mybooks.books.domain;

import jakarta.activation.MimeType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class BookTest {
    private static final BookId ID = new BookId(BookId.BookIdSchema.ISBN, "978-1-83921-196-6");
    private static final String TITLE = "Get Your Hands Dirty on Clean Architecture";
    private static final Author AUTHOR = mock(Author.class);
    private static final Set<Author> AUTHORS = Set.of(AUTHOR);
    private static final Set<String> KEYWORDS = Set.of("architecture", "adapters", "ports");
    private static final Set<MimeType> FORMATS = Set.of(MimeTypes.EPUB);
    public static final Book BOOK = new Book(ID, TITLE, AUTHORS, KEYWORDS, FORMATS);

    @Test
    void constructor() {
        assertAll(
                // error conditions
                () -> assertThrows(NullPointerException.class, () -> new Book(null,null, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(ID, null, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(ID, null, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(ID, TITLE, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(ID, TITLE, AUTHORS, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(ID, TITLE, AUTHORS, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Book(ID, TITLE, AUTHORS, KEYWORDS, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Book(ID, "", AUTHORS, KEYWORDS, FORMATS)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Book(ID, " ", AUTHORS, KEYWORDS, FORMATS)),


                // Description, formats and keywords may be empty
                () -> new Book(ID, TITLE, AUTHORS, KEYWORDS, FORMATS),
                () -> new Book(ID, TITLE, AUTHORS, Set.of(), FORMATS),
                () -> new Book(ID, TITLE, AUTHORS, KEYWORDS, Set.of()),

                // correctly constructed book
                () -> new Book(ID, TITLE, AUTHORS, KEYWORDS, FORMATS)
        );
    }

    @Test
    void addFormat() {
        final var updated = BOOK.addFormat(MimeTypes.PDF);

        assertAll(
                () -> assertNotEquals(BOOK, updated),
                () -> assertEquals(2, updated.formats().size()),
                () -> assertTrue(updated.formats().contains(MimeTypes.PDF)),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> updated.addFormat(MimeTypes.PDF))
        );
    }

    @Test
    void addKeyword() {
        final var updated = BOOK.addKeyword(" Hexagonal");

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> BOOK.addKeyword(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> BOOK.addKeyword(" ")),
                () -> assertNotEquals(BOOK, updated),
                () -> assertEquals(4, updated.keywords().size()),
                () -> assertTrue(updated.keywords().contains("hexagonal")),
                () -> assertThrows(IllegalArgumentException.class, () -> updated.addKeyword("hexagonal"))
        );
    }

    @Test
    void addAuthor() {
        final var me = new Author(AuthorId.withoutId(), Instant.now(), "My Name", Map.of());
        final var updated = BOOK.addAuthor(me);

        assertAll(
                () -> assertNotEquals(BOOK, updated),
                () -> assertEquals(2, updated.authors().size()),
                () -> assertTrue(updated.authors().contains(me)),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> updated.addAuthor(me))
        );
    }
}