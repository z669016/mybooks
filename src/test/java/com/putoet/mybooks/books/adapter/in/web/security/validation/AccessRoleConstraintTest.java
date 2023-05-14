package com.putoet.mybooks.books.adapter.in.web.security.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AccessRoleConstraintTest {
    private AccessRoleConstraint.AccessRoleValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setup() {
        validator = new AccessRoleConstraint.AccessRoleValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void passwordOk() {
        assertAll(
                () -> assertTrue(validator.isValid("user" ,context)),
                () -> assertTrue(validator.isValid("UseR" ,context)),
                () -> assertTrue(validator.isValid("ADMIN" ,context)),
                () -> assertFalse(validator.isValid(null ,context)),
                () -> assertFalse(validator.isValid("bla" ,context))
        );
    }
}
