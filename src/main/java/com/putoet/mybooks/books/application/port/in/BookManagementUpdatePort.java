package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;

import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public interface BookManagementUpdatePort {
    void forgetAuthor(AuthorId authorId);
    Author registerAuthor(String name, Map<SiteType, URL> sites);
    Author setAuthorSite(AuthorId authorId, SiteType type, URL url);
    Author updateAuthor(AuthorId authorId, Instant version, String name);
    Book registerBook(BookId bookId, String title, Set<Author> authors, Set<MimeType> formats, Set<String> keywords);
}
