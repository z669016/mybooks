package com.putoet.mybooks.books.domain.validation;

import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StandardValidationsTest {

    @Test
    void message() {
        assertEquals("must not be blank", StandardValidations.message(NotBlank.class));
    }

    @Test
    void messageFailed() {
        assertThrows(IllegalArgumentException.class, () -> StandardValidations.message(StandardValidations.class));
    }
}