package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.AuthorId;
import com.putoet.mybooks.domain.SiteType;

import java.net.MalformedURLException;
import java.net.URL;

public record SetAuthorSiteCommand(AuthorId authorId, SiteType type, URL url) {
    public SetAuthorSiteCommand {
        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();
        if (type == null)
            ServiceError.AUTHOR_SITE_TYPE_REQUIRED.raise();
        if (url == null)
            ServiceError.AUTHOR_SITE_URL_INVALID.raise();
    }

    public static SetAuthorSiteCommandBuilder withAuthorId(AuthorId id) {
        if (id == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();

        return new SetAuthorSiteCommandBuilder(id);
    }

    public static class SetAuthorSiteCommandBuilder {
        private final AuthorId authorId;
        private SiteType type;
        private URL url;

        private SetAuthorSiteCommandBuilder(AuthorId authorId) {
            if (authorId == null)
                ServiceError.AUTHOR_ID_REQUIRED.raise();

            this.authorId = authorId;
        }

        public SetAuthorSiteCommandBuilder type(SiteType type) {
            if (type == null)
                ServiceError.AUTHOR_SITE_TYPE_REQUIRED.raise();

            this.type = type;
            return this;
        }

        public SetAuthorSiteCommandBuilder type(String type) {
            if (type == null || type.isBlank())
                ServiceError.AUTHOR_SITE_TYPE_REQUIRED.raise();

            this.type = new SiteType(type);
            return this;
        }
        public SetAuthorSiteCommandBuilder url(String url) {
            try {
                return url(new URL(url));
            } catch (MalformedURLException exc) {
                ServiceError.AUTHOR_SITE_URL_INVALID.raise(exc);
            }
            return this;
        }

        public SetAuthorSiteCommandBuilder url(URL url) {
            this.url = url;
            return this;
        }

        public SetAuthorSiteCommand build() {
            return new SetAuthorSiteCommand(authorId, type, url);
        }
    }
}
