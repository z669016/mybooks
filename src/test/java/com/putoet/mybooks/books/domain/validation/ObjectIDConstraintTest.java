package com.putoet.mybooks.books.domain.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectIDConstraintTest {
    private ObjectIDConstraint.ObjectIDValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setup() {
        validator = new ObjectIDConstraint.ObjectIDValidator();
    }

    @Test
    void isValid() {
        assertAll(
                () -> assertFalse(validator.isValid(null, context)),
                () -> assertFalse(validator.isValid("   ", context)),
                () -> assertFalse(validator.isValid("bla", context)),
                () -> assertTrue(validator.isValid(UUID.randomUUID().toString(), context))
        );
    }
}
