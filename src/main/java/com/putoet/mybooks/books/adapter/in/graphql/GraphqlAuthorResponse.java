package com.putoet.mybooks.books.adapter.in.graphql;

import com.putoet.mybooks.books.domain.Author;

import java.util.Collection;
import java.util.stream.Collectors;

public record GraphqlAuthorResponse(String id, String version, String name, Collection<KeyValuePair> sites) {
    public static GraphqlAuthorResponse from(Author author) {
        return new GraphqlAuthorResponse(
                author.id().uuid().toString(),
                author.version().toString(),
                author.name(),
                author.sites().entrySet().stream()
                        .map(entry -> new KeyValuePair(entry.getKey().name(), entry.getValue().toString()))
                        .collect(Collectors.toSet())
        );
    }

    public static Collection<GraphqlAuthorResponse> from(Collection<Author> authors) {
        return authors.stream()
                .map(GraphqlAuthorResponse::from)
                .collect(Collectors.toSet());
    }
}
