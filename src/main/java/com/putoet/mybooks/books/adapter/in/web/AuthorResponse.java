package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.SiteType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Record AuthorResponse
 * The AuthorResponse is a front-end friendly version of the Author entity, where internal representations
 * for domain attributes are replaced by simple string attributes. The static factory methods 'from' translates
 * a domain entity into a user-friendly AuthorBody, where 'toDomain' takes care of the translation into domain
 * entity or domain attributes.
 * @param id String
 * @param version String
 * @param name String
 * @param sites String
 */
public record AuthorResponse(String id, String version, String name, Map<String, String> sites) {
    public static List<AuthorResponse> from(List<Author> domain) {
        return domain.stream().map(AuthorResponse::from).toList();
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

    public static Map<SiteType, URL> toDomain(Map<String, String> sites) {
        if (sites == null)
            return Map.of();

        try {
            final Map<SiteType, URL> domain = new HashMap<>();
            for (String key : sites.keySet()) {
                domain.put(new SiteType(key), new URL(sites.get(key)));
            }
            return domain;
        } catch (MalformedURLException | RuntimeException exc) {
            throw new RuntimeException(exc.getMessage(), exc);
        }
    }
}
