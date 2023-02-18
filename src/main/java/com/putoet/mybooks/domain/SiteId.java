package com.putoet.mybooks.domain;

import java.util.UUID;

public record SiteId(UUID uuid) {
    public static SiteId withoutId() { return new SiteId(UUID.randomUUID()); }
    public static SiteId withId(String uuid) { return new SiteId(UUID.fromString(uuid)); }
}
