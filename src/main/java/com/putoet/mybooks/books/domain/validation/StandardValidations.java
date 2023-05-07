package com.putoet.mybooks.books.domain.validation;

import java.io.IOException;
import java.util.Properties;

public class StandardValidations {
    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(StandardValidations.class.getResourceAsStream("/org/hibernate/validator/ValidationMessages.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String message(Class<?> annotation) {
        return message(annotation.getName());
    }

    public static String message(String annotation) {
        final String message = properties.getProperty(annotation + ".message");
        if (message == null)
            throw new IllegalArgumentException("No default message for annotation " + annotation);

        return message;
    }
}
