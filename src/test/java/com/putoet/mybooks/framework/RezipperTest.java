package com.putoet.mybooks.framework;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RezipperTest {
    private static final Logger logger = LoggerFactory.getLogger(RezipperTest.class);

    @Test
    void bookForFile() {
        final String fileName = "/Users/renevanputten/OneDrive/Documents/Books/Manning Books/Node.js in Action, Second Edition/Node.js_in_Action_S.epub";
        final Optional<String> epub = Rezipper.repackage(fileName);
        assertTrue(epub.isPresent());
        assertNotEquals(fileName, epub.get());
    }
}