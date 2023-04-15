package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.SiteType;

import java.net.URL;
import java.util.Map;

public interface RegisterAuthor {
    Author registerAuthor(String name, Map<SiteType, URL> sites);
}
