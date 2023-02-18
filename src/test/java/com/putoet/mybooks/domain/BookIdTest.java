package com.putoet.mybooks.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookIdTest {

    @Test
    void withoutId() {
        final BookId id1 = BookId.withoutId();
        assertNotNull(id1.uuid());
        final BookId id2 = BookId.withoutId();
        assertNotNull(id2.uuid());
        assertNotEquals(id1, id2);

        System.out.println(id1.uuid() + "(" + id1.uuid().toString().length() + ")");
    }

    @Test
    void withId() {
        final BookId id = BookId.withoutId();
        final BookId copy = BookId.withId(id.uuid().toString());

        assertNotNull(copy.uuid());
        assertEquals(id, copy);
    }
}