package com.putoet.mybooks.books.adapter.in.web.security.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordConstraintTest {
    private PasswordConstraint.MyPasswordValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setup() {
        validator = new PasswordConstraint.MyPasswordValidator();
    }

    @Test
    void passwordOk() {
        assertAll(
                () -> assertTrue(validator.isValid("1abcdef!", context)),
                () -> assertFalse(validator.isValid(null, context)),
                () -> assertFalse(validator.isValid("1abcde!", context)),
                () -> assertFalse(validator.isValid("1234567890abcdefghijklmnopqrstuvwxyz!", context)),
                () -> assertFalse(validator.isValid("1abcdefg", context)),
                () -> assertFalse(validator.isValid("1aaabacadaef!", context))
        );
    }
}
