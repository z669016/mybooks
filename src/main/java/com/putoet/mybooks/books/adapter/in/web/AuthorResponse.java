package com.putoet.mybooks.books.adapter.in.web;

import com.drew.lang.annotations.NotNull;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.SiteType;
import com.putoet.mybooks.books.domain.validation.ObjectIDConstraint;
import com.putoet.mybooks.books.domain.validation.SiteMapConstraint;
import com.putoet.mybooks.books.domain.validation.VersionConstraint;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Record AuthorResponse
 * The AuthorResponse is a front-end friendly version of the Author entity, where internal representations
 * for domain attributes are replaced by simple string attributes. The static factory methods 'from' translates
 * a domain entity into a user-friendly AuthorBody, where 'toDomain' takes care of the translation into domain
 * entity or domain attributes.
 *
 * @param id      String
 * @param version String
 * @param name    String
 * @param sites   String
 */
public record AuthorResponse(
        @ObjectIDConstraint String id,
        @VersionConstraint String version,
        @NotNull String name,
        @SiteMapConstraint Map<String, String> sites
) {
    public static Set<AuthorResponse> from(Collection<Author> domain) {
        return domain.stream().map(AuthorResponse::from).collect(Collectors.toSet());
    }

    public static AuthorResponse from(Author domain) {
        return new AuthorResponse(domain.id().uuid().toString(),
                domain.version().toString(),
                domain.name(),
                domain.sites().entrySet().stream().collect(Collectors.toMap(
                        (Map.Entry<SiteType, URL> entry) -> entry.getKey().name(),
                        (Map.Entry<SiteType, URL> entry) -> entry.getValue().toString()
                )));
    }
}
