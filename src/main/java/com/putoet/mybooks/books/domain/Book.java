package com.putoet.mybooks.books.domain;

import jakarta.activation.MimeType;

import java.util.*;

/**
 * Record Book
 * @param id BookId
 * @param title String
 * @param authors Set of Author
 * @param keywords Set of string keywords
 * @param formats Component containing available mimetypes for book formats
 */
public record Book(BookId id, String title, Set<Author> authors, Set<String> keywords, Set<MimeType> formats) {
    public Book {
        Objects.requireNonNull(id);
        Objects.requireNonNull(title);
        Objects.requireNonNull(authors);
        Objects.requireNonNull(keywords);
        Objects.requireNonNull(formats);

        if (title.isBlank())
            throw new IllegalArgumentException("Book title must not be blank.");
    }

    public Book addFormat(MimeType format) {
        Objects.requireNonNull(format);

        if (formats.contains(format))
            throw new IllegalArgumentException("Book " + id + " already contains format " + format);

        final var formats = new HashSet<>(this.formats);
        formats.add(format);

        return new Book(id, title, authors, keywords, Collections.unmodifiableSet(formats));
    }

    public Book addKeyword(String keyword) {
        Objects.requireNonNull(keyword);

        if (keyword.isBlank())
            throw new IllegalArgumentException("Book keyword must not be blank.");

        keyword = keyword.strip().toLowerCase();
        if (keywords.contains(keyword))
            throw new IllegalArgumentException("Book " + id + " keywords already contains '" + keyword + "'");

        final var keywords = new HashSet<>(this.keywords);
        keywords.add(keyword);
        return new Book(id, title, authors, Collections.unmodifiableSet(keywords), formats);
    }

    public Book addAuthor(Author author) {
        Objects.requireNonNull(author);

        if (authors.contains(author))
            throw new IllegalArgumentException("Book author list already contains '" + author + "'");

        final var authors = new HashSet<>(this.authors);
        authors.add(author);
        return new Book(id, title, Collections.unmodifiableSet(authors), keywords, formats);
    }
}
