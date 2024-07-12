package com.putoet.mybooks.books.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.util.UUID;

@Documented
@Constraint(validatedBy = ObjectIDConstraint.ObjectIDValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectIDConstraint {

    String ID_ERROR = "Invalid ID. ID's are without meaning, and their format follow UUID conventions.";

    String message() default ID_ERROR;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class ObjectIDValidator implements ConstraintValidator<ObjectIDConstraint,String> {
        @Override
        public void initialize(ObjectIDConstraint constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(String id, ConstraintValidatorContext context) {
            try {
                if (id == null || id.isBlank())
                    return false;

                UUID.fromString(id);
                return true;
            } catch (IllegalArgumentException ignored) {
            }

            return false;
        }
    }
}