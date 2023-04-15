package com.putoet.mybooks.books.domain;

import java.net.URL;

/**
 * Record Site
 * Contains the type and URL of a site maintained by an Author
 * @param type SiteType
 * @param url URL
 */
public record Site(SiteType type, URL url) {
}
