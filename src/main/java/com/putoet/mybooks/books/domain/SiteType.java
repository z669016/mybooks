package com.putoet.mybooks.books.domain;

import java.util.Objects;

/**
 * Record SiteType
 * There is a set of predefined site types available including twitter, and linkedin.
 * @param name String
 */
public record SiteType(String name) {
    public static final String GITHUB_NAME = "GitHub";
    public static final String TWITTER_NAME = "Twitter";
    public static final String LINKEDIN_NAME = "LinkedIn";
    public static final String FACEBOOK_NAME = "Facebook";
    public static final String HOMEPAGE_NAME = "Home page";
    public static final String INSTAGRAM_NAME = "Instagram";

    public static final SiteType GITHUB = new SiteType(GITHUB_NAME);
    public static final SiteType TWITTER = new SiteType(TWITTER_NAME);
    public static final SiteType LINKEDIN = new SiteType(LINKEDIN_NAME);
    public static final SiteType FACEBOOK = new SiteType(FACEBOOK_NAME);
    public static final SiteType HOMEPAGE = new SiteType(HOMEPAGE_NAME);
    public static final SiteType INSTAGRAM = new SiteType(INSTAGRAM_NAME);

    public SiteType {
        Objects.requireNonNull(name);
        if (name.isBlank())
            throw new IllegalArgumentException("Site type name must not be blank");
    }
}
