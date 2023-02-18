package com.putoet.mybooks.domain;

import java.net.URL;

public record Site(SiteId id, SiteType type, URL url) {
}
