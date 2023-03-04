package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.SiteType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public record RegisterAuthorCommand(String name, Map<SiteType, URL> sites) {

    public RegisterAuthorCommand {
        if (name == null || name.isBlank())
            RegisterAuthor.error(ServiceError.AUTHOR_NAME_REQUIRED);
        if (sites == null)
            RegisterAuthor.error(ServiceError.AUTHOR_SITE_DETAILS_REQUIRED);
    }

    public static RegisterAuthorCommandBuilder withName(String name) {
        return new RegisterAuthorCommandBuilder(name);
    }

    public static class RegisterAuthorCommandBuilder {
        private final String name;
        private final Map<SiteType,URL> sites = new HashMap<>();

        private RegisterAuthorCommandBuilder(String name) {
            if (name == null || name.isBlank())
                ServiceError.AUTHOR_NAME_REQUIRED.raise();

            this.name = name;
        }

        public RegisterAuthorCommandBuilder withSite(String name, String url) {
            return withSite(new SiteType(name), url);
        }

        public RegisterAuthorCommandBuilder withSite(SiteType type, String url) {
            try {
                return withSite(type, new URL(url));
            } catch (MalformedURLException exc) {
                RegisterAuthor.error(ServiceError.AUTHOR_SITE_URL_INVALID, exc);
            }
            return this;
        }

        public RegisterAuthorCommandBuilder withSite(SiteType type, URL url) {
            if (type == null)
                ServiceError.AUTHOR_SITE_TYPE_REQUIRED.raise();
            if (url == null)
                ServiceError.AUTHOR_SITE_URL_INVALID.raise();

            sites.put(type, url);
            return this;
        }

        public RegisterAuthorCommand build() {
            return new RegisterAuthorCommand(name, sites);
        }
    }
}
