package com.putoet.mybooks.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ISBNTest {
    public static final String ISBN_13_TEXT = "978-0-7645-7682-9";
    public static final String ISBN_10_TEXT = "0-7645-7682-8";

    private final ISBN ISBN_13 = ISBN.withISBN(ISBN_13_TEXT);

    @Test
    void isValid() {
        assertTrue(ISBN.isValid(ISBN_13_TEXT));
        assertTrue(ISBN.isValid(ISBN_10_TEXT));

        // isbn must be valid
        assertFalse(ISBN.isValid("978-0-7645-7682-8"));
        assertFalse(ISBN.isValid("0-7645-7682-7"));

        // isbn must not be null
        assertThrows(NullPointerException.class, () -> ISBN.withISBN(null));
    }

    @Test
    void with() {
        assertEquals(ISBN_13_TEXT, ISBN.withISBN(ISBN_13_TEXT).toString());
        assertEquals(ISBN_13_TEXT, ISBN.withISBN(ISBN_10_TEXT).toString());
    }

    @Test
    void prefix() {
        assertEquals("978", ISBN_13.prefix());
    }

    @Test
    void group() {
        assertEquals("0", ISBN_13.group());
    }

    @Test
    void publisher() {
        assertEquals("7645", ISBN_13.publisher());
    }

    @Test
    void bookName() {
        assertEquals("7682", ISBN_13.bookName());
    }

    @Test
    void checkDigit() {
        assertEquals("9", ISBN_13.checkDigit());
    }
}