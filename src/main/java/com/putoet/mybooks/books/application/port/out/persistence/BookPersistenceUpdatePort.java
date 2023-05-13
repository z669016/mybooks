package com.putoet.mybooks.books.application.port.out.persistence;

import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;

import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public interface BookPersistenceUpdatePort extends BookPersistenceQueryPort {
    Author registerAuthor(String name, Map<SiteType, URL> sites);
    Author updateAuthor(AuthorId authorId, Instant version, String name);
    void forgetAuthor(AuthorId authorId);
    Author setAuthorSite(AuthorId id, SiteType type, URL url);
    Book registerBook(BookId bookId, String title, Set<Author> authors, Set<MimeType> formats, Set<String> keywords);
}

