package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.SiteType;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthorResponseTest {
    private final Author author = new Author(AuthorId.withoutId(), Instant.now(), "Schrijver, Jaap de", Map.of());

    @Test
    void from() {
        final var response = AuthorResponse.from(author);
        final var responses = AuthorResponse.from(Set.of(author));

        assertAll(
                () -> assertEquals(author.id().uuid().toString(), response.id()),
                () -> assertEquals(author.version().toString(), response.version()),
                () -> assertEquals(author.name(), response.name()),
                () -> assertEquals(author.sites().size(), response.sites().size()),

                () -> assertEquals(1, responses.size()),
                () -> assertEquals(response, responses.stream().findFirst().orElseThrow())
        );
    }

    @Test
    void toDomain() throws MalformedURLException {
        final var sites = Map.of(
                SiteType.LINKEDIN_NAME, "https://au.linkedin.com/in/thombergs",
                SiteType.GITHUB_NAME, "https://github.com/thombergs"
        );

        final var domain = Map.of(
                SiteType.of(SiteType.LINKEDIN_NAME), new URL("https://au.linkedin.com/in/thombergs"),
                SiteType.of(SiteType.GITHUB_NAME), new URL("https://github.com/thombergs")
        );

        assertEquals(domain, NewAuthorRequest.sitesWithURLs(sites));
    }
}