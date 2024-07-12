package com.putoet.mybooks.books.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RezipperTest {
    @Test
    void bookForFile() {
        final var fileName = "/Users/renevanputten/OneDrive/Books/Manning Books/Advanced Algorithms and Data Structures/Advanced_Algorithms_and_Data_Structures.epub";
        final var epub = Rezipper.repackage(fileName);

        assertAll(
                () -> assertTrue(epub.isPresent()),
                () -> assertNotEquals(fileName, epub.orElseThrow())
        );
    }
}