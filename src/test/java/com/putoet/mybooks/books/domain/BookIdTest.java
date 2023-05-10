package com.putoet.mybooks.books.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BookIdTest {
    @Test
    void constructor() {
        final BookId bookId = new BookId();

        assertAll(
                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId("", "")),
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId("BLA", "")),
                () -> assertThrows(NullPointerException.class, () -> new BookId(BookId.BookIdScheme.UUID, null)),

                // normal expected outcome
                () -> assertEquals(BookId.BookIdScheme.UUID, bookId.schema()),
                () -> assertNotNull(bookId.id())
        );
    }

    @Test
    void isbnBookId() {
        final String id10 = "99921-58-10-7";
        final String id13 = "978-3-16-148410-0";
        final BookId.BookIdScheme schema = BookId.BookIdScheme.ISBN;

        final BookId bookId10 = new BookId(schema, id10);
        final BookId bookId13 = new BookId(schema, id13);

        assertAll(
                () -> assertEquals(schema, bookId10.schema()),
                () -> assertEquals(id10, bookId10.id()),
                () -> assertEquals(schema, bookId13.schema()),
                () -> assertEquals(id13, bookId13.id()),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "123"))
        );
    }

    @Test
    void uriBookId() {
        final String id = "urn:oasis:names:specification:docbook:dtd:xml:4.1.2";
        final BookId bookId = new BookId(BookId.BookIdScheme.URI, id);
        final BookId.BookIdScheme schema = BookId.BookIdScheme.URI;

        assertAll(
                () -> assertEquals(schema, bookId.schema()),
                () -> assertEquals(id, bookId.id()),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "ftp.{jt-software.net}/style.css"))
        );
    }

    @Test
    void urlBookId() {
        final String id = "https://leanpub.com/wardley-maps";
        final BookId bookId = new BookId(BookId.BookIdScheme.URL, id);
        final BookId.BookIdScheme schema = BookId.BookIdScheme.URL;

        assertAll(
                () -> assertEquals(schema, bookId.schema()),
                () -> assertEquals(id, bookId.id()),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "leanpub.com/wardley-maps"))
        );
    }

    @Test
    void uuidBookId() {
        final String id = UUID.randomUUID().toString();
        final BookId.BookIdScheme schema = BookId.BookIdScheme.UUID;
        final BookId bookId = new BookId(schema, id);

        assertAll(
                () -> assertEquals(schema, bookId.schema()),
                () -> assertEquals(id, bookId.id()),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "123"))
        );
    }
}