package com.putoet.mybooks.books.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthorIdTest {
    @Test
    void withoutId() {
        assertNotNull(AuthorId.withoutId().uuid());
    }

    @Test
    void withId() {
        final String uuid = UUID.randomUUID().toString();
        assertEquals(uuid, AuthorId.withId(uuid).uuid().toString());
    }
}