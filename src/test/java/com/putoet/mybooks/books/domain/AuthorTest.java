package com.putoet.mybooks.books.domain;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorTest {
    private static final AuthorId ID = AuthorId.withoutId();
    private static final String NAME = "Tom Hombergs";
    private static final String BLOG_NAME = "blog";
    private static final Instant NOW = Instant.now();
    private static final Map<SiteType, URL> SITES = new HashMap<>();

    public static final Author AUTHOR = new Author(ID, NOW, NAME, SITES);

    private static URL github;
    private static URL twitter;
    private static URL linkedIn;
    private static URL facebook;
    private static URL homePage;
    private static URL blog;

    static {
        try {
            github = new URL("https://github.com/thombergs");
            linkedIn = new URL("https://au.linkedin.com/in/thombergs");
            twitter = new URL("https://twitter.com/tomhombergs");
            facebook = new URL("https://reflectoring.io/");
            homePage = new URL("https://facebook.com/reflectoring");
            blog = new URL("https://www.freecodecamp.org/news/author/thombergs/");
        } catch (MalformedURLException ignored) {
            // not used
        }

        SITES.put(SiteType.GITHUB, github);
        SITES.put(SiteType.TWITTER, twitter);
        SITES.put(SiteType.LINKEDIN, linkedIn);
        SITES.put(SiteType.FACEBOOK, facebook);
        SITES.put(SiteType.HOMEPAGE, homePage);
        SITES.put(SiteType.of(BLOG_NAME), blog);
    }

    @Test
    void constructor() {
        final var author = new Author(ID, NAME);

        assertAll(
                // check error conditions
                () -> assertThrows(NullPointerException.class, () -> new Author(null, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Author(ID, null, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Author(ID, NOW, null, null)),
                () -> assertThrows(NullPointerException.class, () -> new Author(ID, NOW, NAME, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Author(ID, NOW, "  ", SITES)),

                // check constructor
                () -> assertEquals(ID, author.id()),
                () -> assertEquals(NAME, author.name()),
                () -> assertEquals(0, author.sites().size())
        );
    }

    @Test
    void github() {
        assertEquals(github, AUTHOR.github().map(Site::url).orElseThrow());
    }

    @Test
    void twitter() {
        assertEquals(twitter, AUTHOR.twitter().map(Site::url).orElseThrow());
    }

    @Test
    void facebook() {
        assertEquals(facebook, AUTHOR.facebook().map(Site::url).orElseThrow());
    }

    @Test
    void linkedIn() {
        assertEquals(linkedIn, AUTHOR.linkedIn().map(Site::url).orElseThrow());
    }

    @Test
    void homePage() {
        assertEquals(homePage, AUTHOR.homePage().map(Site::url).orElseThrow());
    }

    @Test
    void site() {
        assertEquals(blog, AUTHOR.site(new SiteType(BLOG_NAME)).map(Site::url).orElseThrow());
    }
}