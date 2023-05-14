package com.putoet.mybooks.books.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;
import java.time.format.DateTimeParseException;

@Documented
@Constraint(validatedBy = VersionConstraint.VersionValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface VersionConstraint {

    String VERSION_ERROR = "Version is required, and is encoded as a valid timestamp in the past.";

    String message() default VERSION_ERROR;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class VersionValidator implements ConstraintValidator<VersionConstraint,String> {
        @Override
        public void initialize(VersionConstraint constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(String version, ConstraintValidatorContext context) {
            try {
                if (version == null)
                    return false;

                Instant.parse(version);
                return true;
            } catch (DateTimeParseException ignored) {
            }

            return false;
        }
    }
}