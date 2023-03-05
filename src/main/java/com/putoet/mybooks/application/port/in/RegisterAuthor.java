package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.SiteType;

import java.net.URL;
import java.util.Map;

public interface RegisterAuthor {
    Author registerAuthor(String name, Map<SiteType, URL> sites);
}
