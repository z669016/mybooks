package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.SiteType;

import java.net.URL;

public interface SetAuthorSite {
    Author setAuthorSite(AuthorId authorId, SiteType type, URL url);
}
