package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import com.putoet.mybooks.domain.SiteType;

import java.net.URL;

public interface SetAuthorSite {
    Author setAuthorSite(AuthorId authorId, SiteType type, URL url);
}
