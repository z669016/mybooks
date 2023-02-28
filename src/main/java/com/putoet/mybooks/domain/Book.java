package com.putoet.mybooks.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Book(BookId id, String title, List<Author> authors, String description, List<String> keywords, List<FormatType> formats) {
    public Book {
        Objects.requireNonNull(id);
        Objects.requireNonNull(title);
        Objects.requireNonNull(authors);
        Objects.requireNonNull(description);
        Objects.requireNonNull(keywords);
        Objects.requireNonNull(formats);

        if (title.isBlank())
            throw new IllegalArgumentException("Book title must not be blank.");

        if (authors.isEmpty())
            throw new IllegalArgumentException("Book author list must not be empty.");
    }

    public Book addFormat(FormatType format) {
        Objects.requireNonNull(format);

        if (formats.contains(format))
            throw new IllegalArgumentException("Book already contains format " + format);

        var updated = new ArrayList<>(formats);
        updated.add(format);
        return new Book(id, title, authors, description, keywords, updated);
    }

    public Book addKeyword(String keyword) {
        Objects.requireNonNull(keyword);
        if (keyword.isBlank())
            throw new IllegalArgumentException("Book keyword must not be blank.");

        keyword = keyword.strip().toLowerCase();
        if (keywords.contains(keyword))
            throw new IllegalArgumentException("Book keywords already contains '" + keyword + "'");

        var updated = new ArrayList<>(keywords);
        updated.add(keyword);
        return new Book(id, title, authors, description, updated, formats);
    }

    public Book addAuthor(Author author) {
        Objects.requireNonNull(author);

        if (authors.contains(author))
            throw new IllegalArgumentException("Book author list already contains '" + author + "'");

        var updated = new ArrayList<>(authors);
        updated.add(author);
        return new Book(id, title, updated, description, keywords, formats);
    }

    public Book description(String description) {
        Objects.requireNonNull(description);

        return new Book(id, title, authors, description, keywords, formats);
    }
}
