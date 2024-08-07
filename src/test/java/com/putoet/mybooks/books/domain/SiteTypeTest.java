package com.putoet.mybooks.books.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SiteTypeTest {
    @Test
    void constructor() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> SiteType.of(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> new SiteType(" "))
        );
    }

    @Test
    void building() {
        assertAll(
                () -> assertEquals(SiteType.GITHUB_NAME, SiteType.GITHUB.name()),
                () -> assertEquals(SiteType.TWITTER_NAME, SiteType.TWITTER.name()),
                () -> assertEquals(SiteType.LINKEDIN_NAME, SiteType.LINKEDIN.name()),
                () -> assertEquals(SiteType.FACEBOOK_NAME, SiteType.FACEBOOK.name()),
                () -> assertEquals(SiteType.HOMEPAGE_NAME, SiteType.HOMEPAGE.name()),
                () -> assertEquals(SiteType.INSTAGRAM_NAME, SiteType.INSTAGRAM.name())
        );
    }
}