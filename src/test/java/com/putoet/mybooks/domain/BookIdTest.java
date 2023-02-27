package com.putoet.mybooks.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookIdTest {
    @Test
    void nullParameters() {
        assertThrows(NullPointerException.class, () -> new BookId(null, ""));
        assertThrows(NullPointerException.class, () -> new BookId(BookId.BookIdScheme.UUID, null));
    }

    @Test
    void defaultBookId() {
        final BookId bookId = new BookId();
        assertEquals(BookId.BookIdScheme.UUID, bookId.schema());
        assertNotNull(bookId.id());
    }

    @Test
    void isbnBookId() {
        final String id10 = "99921-58-10-7";
        final String id13 = "978-3-16-148410-0";
        final BookId.BookIdScheme schema = BookId.BookIdScheme.ISBN;

        BookId bookId = new BookId(schema, id10);
        assertEquals(schema, bookId.schema());
        assertEquals(id10, bookId.id());

        bookId = new BookId(schema, id13);
        assertEquals(schema, bookId.schema());
        assertEquals(id13, bookId.id());

        assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "123"));
    }

    @Test
    void uriBookId() {
        final String id = "urn:oasis:names:specification:docbook:dtd:xml:4.1.2";
        final BookId bookId = new BookId(BookId.BookIdScheme.URI, id);
        final BookId.BookIdScheme schema = BookId.BookIdScheme.URI;

        assertEquals(schema, bookId.schema());
        assertEquals(id, bookId.id());

        assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "ftp.{jt-software.net}/style.css"));
    }

    @Test
    void urlBookId() {
        final String id = "https://leanpub.com/wardley-maps";
        final BookId bookId = new BookId(BookId.BookIdScheme.URL, id);
        final BookId.BookIdScheme schema = BookId.BookIdScheme.URL;

        assertEquals(schema, bookId.schema());
        assertEquals(id, bookId.id());

        assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "leanpub.com/wardley-maps"));
    }

    @Test
    void uuidBookId() {
        final String id = UUID.randomUUID().toString();
        final BookId.BookIdScheme schema = BookId.BookIdScheme.UUID;

        final BookId bookId = new BookId(schema, id);
        assertEquals(schema, bookId.schema());
        assertEquals(id, bookId.id());

        assertThrows(IllegalArgumentException.class, () -> new BookId(schema, "123"));
    }
}