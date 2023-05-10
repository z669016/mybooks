package com.putoet.mybooks.books.adapter.in.web;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookRequestAuthorTest {

    @Test
    void existingAuthorRequest() {
        final BookRequestAuthor bookRequestAuthor = new BookRequestAuthor(UUID.randomUUID().toString(), null, null);
        assertAll(
                () -> assertTrue(bookRequestAuthor.isExistingRequest()),
                () -> assertFalse(bookRequestAuthor.isNewRequest()),
                () -> assertEquals(bookRequestAuthor.id(), bookRequestAuthor.existingAuthorRequest().id()),
                () -> assertThrows(IllegalStateException.class, bookRequestAuthor::newAuthorRequest)
        );
    }

    @Test
    void newAuthorRequest() {
        final BookRequestAuthor bookRequestAuthor = new BookRequestAuthor(null, "name", Map.of("Google", "https://www.google.com"));
        assertAll(
                () -> assertTrue(bookRequestAuthor.isNewRequest()),
                () -> assertFalse(bookRequestAuthor.isExistingRequest()),
                () -> assertEquals(bookRequestAuthor.name(), bookRequestAuthor.newAuthorRequest().name()),
                () -> assertEquals(bookRequestAuthor.sites(), bookRequestAuthor.newAuthorRequest().sites()),
                () -> assertThrows(IllegalStateException.class, bookRequestAuthor::existingAuthorRequest)
        );
    }
}