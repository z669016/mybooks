package com.putoet.mybooks.books.adapter.in.web.validation;

import com.putoet.mybooks.books.adapter.in.web.BookRequestAuthor;
import com.putoet.mybooks.books.adapter.in.web.ExistingAuthorRequest;
import com.putoet.mybooks.books.adapter.in.web.NewAuthorRequest;
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
            try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
                final Validator validator = factory.getValidator();

                if (bookRequestAuthor.isNewRequest()) {
                    final NewAuthorRequest newAuthorRequest = bookRequestAuthor.newAuthorRequest();
                    final var result = validator.validate(newAuthorRequest);
                    return result.size() == 0;
                } else if (bookRequestAuthor.isExistingRequest()) {
                    final ExistingAuthorRequest existingAuthorRequest = bookRequestAuthor.existingAuthorRequest();
                    final var result = validator.validate(existingAuthorRequest);
                    return result.size() == 0;
                }

                return false;
            } catch (RuntimeException ignored) {
            }

            return false;
        }
    }
}