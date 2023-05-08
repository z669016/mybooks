package com.putoet.mybooks.books.adapter.in.web.security.validation;

import org.junit.jupiter.api.Test;
import org.passay.PasswordData;

import static org.junit.jupiter.api.Assertions.*;

public class AccessRoleValidatorTest {
    @Test
    void passwordOk() {
        assertTrue(PasswordConstraint.MyPasswordValidator.PASSWORD_VALIDATOR.validate(new PasswordData("1abcdef!")).isValid());
    }

    @Test
    void passwordTooShort() {
        var result = PasswordConstraint.MyPasswordValidator.PASSWORD_VALIDATOR.validate(new PasswordData("1abcde!"));
        assertFalse(result.isValid());
        assertEquals(1, result.getDetails().size());
        assertEquals("TOO_SHORT", result.getDetails().get(0).getErrorCode());
    }

    @Test
    void passwordTooLong() {
        var result = PasswordConstraint.MyPasswordValidator.PASSWORD_VALIDATOR.validate(new PasswordData("1234567890abcdefghijklmnopqrstuvwxyz!"));
        assertFalse(result.isValid());
        assertEquals(1, result.getDetails().size());
        assertEquals("TOO_LONG", result.getDetails().get(0).getErrorCode());
    }

    @Test
    void passwordNoSpecialCharacter() {
        var result = PasswordConstraint.MyPasswordValidator.PASSWORD_VALIDATOR.validate(new PasswordData("1abcdefg"));
        assertFalse(result.isValid());
        assertEquals(1, result.getDetails().size());
        assertEquals("INSUFFICIENT_SPECIAL", result.getDetails().get(0).getErrorCode());
    }

    @Test
    void passwordNoNumbers() {
        var result = PasswordConstraint.MyPasswordValidator.PASSWORD_VALIDATOR.validate(new PasswordData("#abcdef!"));
        assertFalse(result.isValid());
        assertEquals(1, result.getDetails().size());
        assertEquals("INSUFFICIENT_DIGIT", result.getDetails().get(0).getErrorCode());
    }

    @Test
    void passwordRepetitiveCharacters() {
        var result = PasswordConstraint.MyPasswordValidator.PASSWORD_VALIDATOR.validate(new PasswordData("1aaaadef!"));
        assertFalse(result.isValid());
        assertEquals(1, result.getDetails().size());
        assertEquals("ILLEGAL_MATCH", result.getDetails().get(0).getErrorCode());
    }
}
