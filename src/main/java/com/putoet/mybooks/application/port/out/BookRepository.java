package com.putoet.mybooks.application.port.out;

import com.putoet.mybooks.domain.*;
import jakarta.activation.MimeType;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BookRepository extends BookInquiryRepository {
    Author registerAuthor(String name, Map<SiteType, URL> sites);
    Author updateAuthor(AuthorId authorId, String name);
    void forgetAuthor(AuthorId authorId);
    Author setAuthorSite(AuthorId id, SiteType type, URL url);
    Book registerBook(BookId bookId, String title, List<Author> authors, MimeTypes formats, Set<String> keywords);
}

