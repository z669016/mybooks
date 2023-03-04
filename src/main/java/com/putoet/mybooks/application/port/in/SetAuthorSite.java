package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;

public interface SetAuthorSite {
    Author setAuthorSite(SetAuthorSiteCommand command);
}
