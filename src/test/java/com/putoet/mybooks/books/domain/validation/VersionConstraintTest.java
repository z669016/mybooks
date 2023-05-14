package com.putoet.mybooks.books.domain.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class VersionConstraintTest {
    private VersionConstraint.VersionValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setup() {
        validator = new VersionConstraint.VersionValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void isValid() {
        assertAll(
                () -> assertFalse(validator.isValid(null, context)),
                () -> assertFalse(validator.isValid("bla", context)),
                () -> assertTrue(validator.isValid(Instant.now().toString(), context))
        );
    }
}
