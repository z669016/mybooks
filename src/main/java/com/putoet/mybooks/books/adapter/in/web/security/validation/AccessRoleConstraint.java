package com.putoet.mybooks.books.adapter.in.web.security.validation;

import com.putoet.mybooks.books.domain.security.AccessRole;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = AccessRoleConstraint.AccessRoleValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessRoleConstraint {

    String ACCESS_ROLE_ERROR = "Invalid access role";

    String message() default ACCESS_ROLE_ERROR;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class AccessRoleValidator implements ConstraintValidator<AccessRoleConstraint, String> {
        @Override
        public void initialize(AccessRoleConstraint constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(String accessRole, ConstraintValidatorContext context) {
            try {
                if (accessRole == null)
                    return false;

                AccessRole.from(accessRole);
                return true;
            } catch (IllegalArgumentException ignored) {
            }

            return false;
        }
    }
}