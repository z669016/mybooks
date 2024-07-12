package com.putoet.mybooks.books.adapter.in.web.validation;

import com.putoet.mybooks.books.adapter.in.web.BookRequestAuthor;
import jakarta.validation.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = BookRequestAuthorConstraint.BookRequestAuthorValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface BookRequestAuthorConstraint {

    String AUTHOR_REQUEST_ERROR = "Author on book creation must be known (id is required) or new (name and sites must be provided)";

    String message() default AUTHOR_REQUEST_ERROR;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class BookRequestAuthorValidator implements ConstraintValidator<BookRequestAuthorConstraint, BookRequestAuthor> {
        @Override
        public void initialize(BookRequestAuthorConstraint constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(BookRequestAuthor bookRequestAuthor, ConstraintValidatorContext context) {
            try (var factory = Validation.buildDefaultValidatorFactory()) {
                final var validator = factory.getValidator();

                if (bookRequestAuthor.isNewRequest()) {
                    final var newAuthorRequest = bookRequestAuthor.newAuthorRequest();
                    final var result = validator.validate(newAuthorRequest);
                    return result.isEmpty();
                } else if (bookRequestAuthor.isExistingRequest()) {
                    final var existingAuthorRequest = bookRequestAuthor.existingAuthorRequest();
                    final var result = validator.validate(existingAuthorRequest);
                    return result.isEmpty();
                }

                return false;
            } catch (RuntimeException ignored) {
            }

            return false;
        }
    }
}