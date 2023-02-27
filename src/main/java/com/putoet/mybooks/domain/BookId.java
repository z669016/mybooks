package com.putoet.mybooks.domain;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public record BookId(BookIdScheme schema, String id) {
    public enum BookIdScheme {
        UUID,
        ISBN,
        URL,
        URI
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public BookId {
        Objects.requireNonNull(schema, "BookId schema must not be null");
        Objects.requireNonNull(id, "BookId id must not be null");

        switch (schema) {
            case UUID -> UUID.fromString(id);
            case URI -> {
                try {
                    new URI(id);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid URI: " + id, e);
                }
            }
            case URL -> {
                try {
                    new URL(id);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Invalid URL: " + id, e);
                }
            }
            case ISBN -> {
                if (!ISBN.isValid(id))
                    throw new IllegalArgumentException("Invalid ISBN: " + id);
            }
        }
    }

    public BookId() {
        this(BookIdScheme.UUID, UUID.randomUUID().toString());
    }
}
