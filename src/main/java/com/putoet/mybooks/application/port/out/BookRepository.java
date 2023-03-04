package com.putoet.mybooks.application.port.out;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import com.putoet.mybooks.domain.SiteType;

import java.net.URL;
import java.util.Map;

public interface BookRepository extends BookInquiryRepository {
    Author createAuthor(String name, Map<SiteType, URL> sites);
    Author updateAuthor(AuthorId authorId, String name);
    void forgetAuthor(AuthorId authorId);
    Author setAuthorSite(AuthorId id, SiteType type, URL url);
}
