package com.putoet.mybooks.books.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BookIdTest {
    @Test
    void constructor() {
        final var bookId = new BookId();

        assertAll(
                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId("", "")),
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId("BLA", "")),
                () -> assertThrows(NullPointerException.class, () -> new BookId(BookId.BookIdSchema.UUID, null)),

                // normal expected outcome
                () -> assertEquals(BookId.BookIdSchema.UUID, bookId.schema()),
                () -> assertNotNull(bookId.id())
        );
    }

    @Test
    void isbnBookId() {
        final var id10 = "99921-58-10-7";
        final var id13 = "978-3-16-148410-0";
        final var schema = BookId.BookIdSchema.ISBN;

        final var bookId10 = new BookId(schema, id10);
        final var bookId13 = new BookId(schema, id13);

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
        final var id = "urn:oasis:names:specification:docbook:dtd:xml:4.1.2";
        final var bookId = new BookId(BookId.BookIdSchema.URI, id);
        final var schema = BookId.BookIdSchema.URI;

        assertAll(
                () -> assertEquals(schema, bookId.schema()),
                () -> assertEquals(id, bookId.id()),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "ftp.{jt-software.net}/style.css"))
        );
    }

    @Test
    void urlBookId() {
        final var id = "https://leanpub.com/wardley-maps";
        final var bookId = new BookId(BookId.BookIdSchema.URL, id);
        final var schema = BookId.BookIdSchema.URL;

        assertAll(
                () -> assertEquals(schema, bookId.schema()),
                () -> assertEquals(id, bookId.id()),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "leanpub.com/wardley-maps"))
        );
    }

    @Test
    void uuidBookId() {
        final var id = UUID.randomUUID().toString();
        final var schema = BookId.BookIdSchema.UUID;
        final var bookId = new BookId(schema, id);

        assertAll(
                () -> assertEquals(schema, bookId.schema()),
                () -> assertEquals(id, bookId.id()),

                // error conditions
                () -> assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "123"))
        );
    }
}