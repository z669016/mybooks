package com.putoet.mybooks.books.domain;

import java.util.UUID;

/**
 * Record AuthorId, identity of an Author
 * @param uuid UUID
 */
public record AuthorId(UUID uuid) {
    public static AuthorId withoutId() { return new AuthorId(UUID.randomUUID()); }
    public static AuthorId withId(String uuid) { return new AuthorId(UUID.fromString(uuid)); }
}
