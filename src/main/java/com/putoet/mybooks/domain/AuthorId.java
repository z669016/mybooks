package com.putoet.mybooks.domain;

import java.util.UUID;

public record AuthorId(UUID uuid) {
    public static AuthorId withoutId() { return new AuthorId(UUID.randomUUID()); }
    public static AuthorId withId(String uuid) { return new AuthorId(UUID.fromString(uuid)); }
}
