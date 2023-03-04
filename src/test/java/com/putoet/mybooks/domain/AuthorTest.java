package com.putoet.mybooks.domain;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthorTest {
    private static final AuthorId id = AuthorId.withoutId();
    private static final String name = "Tom Hombergs";
    private static final String blog_name = "blog";
    private static final Map<SiteType, URL> sites = new HashMap<>();

    public static final Author AUTHOR = new Author(id, name, sites);

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
        }

        sites.put(SiteType.GITHUB, github);
        sites.put(SiteType.TWITTER, twitter);
        sites.put(SiteType.LINKEDIN, linkedIn);
        sites.put(SiteType.FACEBOOK, facebook);
        sites.put(SiteType.HOMEPAGE, homePage);
        sites.put(new SiteType(blog_name), blog);
    }

    @Test
    void constructor() {
        // attributes must not be null
        assertThrows(NullPointerException.class, () -> new Author(null, null, null));
        assertThrows(NullPointerException.class, () -> new Author(id, null, null));
        assertThrows(NullPointerException.class, () -> new Author(id, name, null));

        // name must not be blank
        assertThrows(IllegalArgumentException.class, () -> new Author(id, "  ", sites));

        // should be fine
        final Author author = new Author(id, name);
        assertEquals(id, author.id());
        assertEquals(name, author.name());
        assertEquals(0, author.sites().size());
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
        assertEquals(blog, AUTHOR.site(new SiteType(blog_name)).map(Site::url).orElseThrow());
    }
}