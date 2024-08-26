package com.putoet.mybooks.books.domain.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SiteMapConstraintTest {
    private SiteMapConstraint.SiteMapValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setup() {
        validator = new SiteMapConstraint.SiteMapValidator();
    }

    @Test
    void isValid() {
        assertAll(
                () -> assertFalse(validator.isValid(null, context)),
                () -> assertTrue(validator.isValid(Map.of(), context)),
                () -> assertFalse(validator.isValid(Map.of("   ", "https://www.google.com"), context)),
                () -> assertFalse(validator.isValid(Map.of("bla", "www.google.com"), context)),
                () -> assertTrue(validator.isValid(Map.of("bla", "https://www.google.com"), context))
        );
    }
}
