package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.SiteType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record Author(String id, String version, String name, Map<String, String> sites) {
    public static List<Author> fromDomain(List<com.putoet.mybooks.books.domain.Author> domain) {
        return domain.stream().map(Author::fromDomain).toList();
    }

    public static Author fromDomain(com.putoet.mybooks.books.domain.Author domain) {
        return new Author(domain.id().uuid().toString(),
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

    public static com.putoet.mybooks.books.domain.Author toDomain(Author author) {
        return new com.putoet.mybooks.books.domain.Author(
                AuthorId.withId(author.id),
                Instant.parse(author.version),
                author.name,
                toDomain(author.sites));
    }
}
