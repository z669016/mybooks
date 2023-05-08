package com.putoet.mybooks.books.adapter.in.web.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.passay.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = PasswordConstraint.MyPasswordValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordConstraint {

    String PASSWORD_ERROR = "Password must be minimal 8-32 characters long, may not contain whitespace, contain at least 1 number, 1 special character, and no more than 3 repeating characters";

    String message() default PASSWORD_ERROR;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class MyPasswordValidator implements ConstraintValidator<PasswordConstraint,String> {
        public static final PasswordValidator PASSWORD_VALIDATOR = new PasswordValidator(
                new LengthRule(8, 32),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule(),
                new RepeatCharacterRegexRule(3)
                );

        @Override
        public void initialize(PasswordConstraint constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(String password, ConstraintValidatorContext context) {
            if (password == null)
                return false;

            return PASSWORD_VALIDATOR.validate(new PasswordData(password)).isValid();
        }
    }
}