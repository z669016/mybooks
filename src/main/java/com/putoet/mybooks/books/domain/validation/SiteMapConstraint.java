package com.putoet.mybooks.books.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Documented
@Constraint(validatedBy = SiteMapConstraint.SiteMapValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface SiteMapConstraint {

    String SITEMAP_ERROR = "Site map is required, and must contain non blank site names, and valid URL values for each key.";

    String message() default SITEMAP_ERROR;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class SiteMapValidator implements ConstraintValidator<SiteMapConstraint, Map<String, String>> {
        @Override
        public void initialize(SiteMapConstraint constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(Map<String, String> sites, ConstraintValidatorContext context) {
            if (sites == null)
                return false;

            boolean valid = true;
            try {
                for (String site : sites.keySet()) {
                    if (site == null || site.isBlank())
                        return false;

                    final String url = sites.get(site);
                    new URL(url);
                }
            } catch (MalformedURLException exc) {
                valid = false;
            }

            return valid;
        }
    }
}