package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Site;
import com.putoet.mybooks.domain.SiteId;
import com.putoet.mybooks.domain.SiteType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public record RegisterAuthorCommand(String name, Map<SiteType, Site> sites) {
    public RegisterAuthorCommand {
        Objects.requireNonNull(name);
        Objects.requireNonNull(sites);
        if (name.isBlank())
            throw new IllegalArgumentException("Author name must not be blank.");
    }

    public static RegisterAuthorCommandBuilder withName(String name) {
        return new RegisterAuthorCommandBuilder(name);
    }

    public static class RegisterAuthorCommandBuilder {
        private final String name;
        private final Map<SiteType,Site> sites = new HashMap<>();

        private RegisterAuthorCommandBuilder(String name) {
            this.name = name;
        }

        public RegisterAuthorCommandBuilder withSite(String name, String url) {
            return withSite(SiteType.OTHER(name), url);
        }

        public RegisterAuthorCommandBuilder withSite(SiteType type, String url) {
            try {
                return withSite(type, new URL(url));
            } catch (MalformedURLException exc) {
                throw new IllegalArgumentException("Invalid site URL '" + url + "'", exc);
            }
        }

        public RegisterAuthorCommandBuilder withSite(SiteType type, URL url) {
            sites.put(type, new Site(SiteId.withoutId(), type, url));
            return this;
        }

        public RegisterAuthorCommand build() {
            return new RegisterAuthorCommand(name, sites);
        }
    }
}
