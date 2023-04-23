package com.putoet.mybooks.books.domain;

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

    public BookId(String schema, String id) {
        this(BookIdScheme.valueOf(schema.trim().toUpperCase()), id);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public BookId {
        Objects.requireNonNull(schema, "BookId schema must not be null");
        Objects.requireNonNull(id, "BookId id must not be null");

        switch (schema) {
            case UUID -> {
                try {
                    id = id.toLowerCase();
                    id = id.replace("urn:","");
                    id = id.replace("uuid:","");

                    UUID.fromString(id);
                } catch (RuntimeException exc) {
                    throw new IllegalArgumentException("Invalid UUID '" + id + "'", exc);
                }
            }
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
