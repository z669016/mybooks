package com.putoet.mybooks.books.adapter.in.graphql;

import com.putoet.mybooks.books.domain.Book;
import jakarta.activation.MimeType;

import java.util.Collection;
import java.util.stream.Collectors;

public record GraphqlBookResponse(String schema, String id, String title, Collection<GraphqlAuthorResponse> authors, Collection<String> keywords, Collection<String> formats) {
    public static GraphqlBookResponse from(Book book) {
        return new GraphqlBookResponse(
                book.id().schema().name(),
                book.id().id(),
                book.title(),
                GraphqlAuthorResponse.from(book.authors()),
                book.keywords(),
                book.formats().stream()
                        .map(MimeType::toString)
                        .collect(Collectors.toSet())
        );
    }

    public static Collection<GraphqlBookResponse> from(Collection<Book> books) {
        return books.stream()
                .map(GraphqlBookResponse::from)
                .collect(Collectors.toSet());
    }
}
