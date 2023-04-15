package com.putoet.mybooks.books.domain;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Record Author
 * @param id AuthorId - unique identification for the author
 * @param name String - lastname, firstname
 * @param sites Map<SiteType,Site> - map of website references for the author
 */
public record Author(AuthorId id, String name, Map<SiteType, URL> sites) {
    public Author {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(sites);

        if (name.isBlank())
            throw new IllegalArgumentException("Author name must not be blank.");
    }

    public Author(AuthorId id, String name) {
        this(id, name, Map.of());
    }

    public Optional<Site> github() {
        return site(SiteType.GITHUB);
    }

    public Optional<Site> twitter() {
        return site(SiteType.TWITTER);
    }

    public Optional<Site> facebook() {
        return site(SiteType.FACEBOOK);
    }

    public Optional<Site> linkedIn() {
        return site(SiteType.LINKEDIN);
    }

    public Optional<Site> homePage() {
        return site(SiteType.HOMEPAGE);
    }

    public Optional<Site> site(SiteType type) {

        return !sites.containsKey(type) ? Optional.empty() : Optional.of(new Site(type, sites.get(type)));
    }
}
