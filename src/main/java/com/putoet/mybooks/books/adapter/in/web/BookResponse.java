package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.Book;
import com.putoet.mybooks.books.domain.MimeTypes;
import jakarta.activation.MimeType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 *  The BookResponse is a front-end friendly version of the Book entity, where internal representations
 *  * for domain attributes are replaced by simple string attributes. The static factory methods 'from' translates
 *  * a domain entity into a user-friendly BookResponse.
 * @param schema String
 * @param id String
 * @param title String
 * @param authors List of AuthorResponse
 * @param keywords String
 * @param formats List of String (mime type)
 */
public record BookResponse(String schema, String id, String title, Set<AuthorResponse> authors, Set<String> keywords, Set<String> formats) {
    public static Set<BookResponse> from(Set<Book> books) {
        return books.stream()
                .map(BookResponse::from)
                .collect(Collectors.toSet());
    }

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.id().schema().name(),
                book.id().id(),
                book.title(),
                AuthorResponse.from(book.authors()),
                book.keywords(),
                book.formats().mimeTypes().stream().map(Object::toString).collect(Collectors.toSet())
        );
    }

    public static Set<MimeType> toDomain(Set<String> formats) {
        return formats.stream()
                .map(MimeTypes::toMimeType)
                .collect(Collectors.toSet());
    }
}
