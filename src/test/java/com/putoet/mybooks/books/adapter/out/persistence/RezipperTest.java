package com.putoet.mybooks.books.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RezipperTest {
    @Test
    void bookForFile() {
        final String fileName = "/Users/renevanputten/OneDrive/Documents/Books/Manning Books/Node.js in Action, Second Edition/Node.js_in_Action_S.epub";
        final Optional<String> epub = Rezipper.repackage(fileName);

        assertAll(
                () -> assertTrue(epub.isPresent()),
                () -> assertNotEquals(fileName, epub.get())
        );
    }
}