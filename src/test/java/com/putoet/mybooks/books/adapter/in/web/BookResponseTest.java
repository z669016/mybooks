package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookResponseTest {
    private final Author author = new Author(AuthorId.withoutId(), Instant.now(), "Schrijver, Jaap de", Map.of());
    private final List<String> formats = List.of(MimeTypes.PDF.toString(), MimeTypes.EPUB.toString());
    private final Book book = new Book(new BookId(BookId.BookIdScheme.ISBN, "978-1-83921-196-6"),
            "Get Your Hands Dirty on Clean Architecture",
            List.of(author),
            Set.of("architecture", "rest"),
            new MimeTypes(BookResponse.toDomain(formats))
    );

    @Test
    void toDomain() {
        final List<MimeType> list = List.of(MimeTypes.PDF, MimeTypes.EPUB);
        assertEquals(list, BookResponse.toDomain(List.of(MimeTypes.PDF.toString(), MimeTypes.EPUB.toString())));
    }

    @Test
    void fromBook() {
        final BookResponse response = BookResponse.from(book);
        assertEquals(book.id().schema().name(), response.schema());
        assertEquals(book.id().id(), response.id());
        assertEquals(book.title(), response.title());
        assertEquals(AuthorResponse.from(book.authors()), response.authors());
        assertEquals(book.keywords(), response.keywords());
        assertEquals(formats, response.formats());

        final List<BookResponse> responses = BookResponse.from(List.of(book));
        assertEquals(1, responses.size());
        assertEquals(response, responses.get(0));
    }
}