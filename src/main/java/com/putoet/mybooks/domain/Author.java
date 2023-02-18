package com.putoet.mybooks.domain;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record Author(AuthorId id, String name, Map<SiteType,Site> sites) {
    public Author {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(sites);

        if (name.isBlank())
            throw new IllegalArgumentException("Author name must not be blank.");
    }

    public Optional<Site> github() {
        return Optional.ofNullable(sites.get(SiteType.GITHUB));
    }

    public Optional<Site> twitter() {
        return Optional.ofNullable(sites.get(SiteType.TWITTER));
    }

    public Optional<Site> facebook() {
        return Optional.ofNullable(sites.get(SiteType.FACEBOOK));
    }

    public Optional<Site> linkedIn() {
        return Optional.ofNullable(sites.get(SiteType.LINKEDIN));
    }

    public Optional<Site> homePage() {
        return Optional.ofNullable(sites.get(SiteType.HOMEPAGE));
    }

    public Optional<Site> site(String name) {
        return Optional.ofNullable(sites.get(SiteType.OTHER(name)));
    }

    @Override
    public String toString() {
        return name;
    }
}
