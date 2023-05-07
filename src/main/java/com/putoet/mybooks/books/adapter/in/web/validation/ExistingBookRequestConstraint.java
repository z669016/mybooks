package com.putoet.mybooks.books.adapter.in.web.validation;

import com.putoet.mybooks.books.adapter.in.web.ExistingBookRequest;
import com.putoet.mybooks.books.domain.BookId;
import jakarta.validation.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = ExistingBookRequestConstraint.ExistingBookRequestValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistingBookRequestConstraint {

    String BOOK_REQUEST_ERROR = "Book id schema must be a valid schema (e.g. ISBN, or UUID) and id value must match the schema";

    String message() default BOOK_REQUEST_ERROR;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class ExistingBookRequestValidator implements ConstraintValidator<ExistingBookRequestConstraint, ExistingBookRequest> {
        @Override
        public void initialize(ExistingBookRequestConstraint constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(ExistingBookRequest bookRequest, ConstraintValidatorContext context) {
            try {
                new BookId(bookRequest.schema(), bookRequest.id());
                return true;
            } catch (IllegalArgumentException ignored) {
            }

            return false;
        }
    }
}