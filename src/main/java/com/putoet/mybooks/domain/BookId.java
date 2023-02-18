package com.putoet.mybooks.domain;

import java.util.UUID;

public record BookId(UUID uuid) {
    public static BookId withoutId() { return new BookId(UUID.randomUUID()); }
    public static BookId withId(String uuid) { return new BookId(UUID.fromString(uuid)); }
}
