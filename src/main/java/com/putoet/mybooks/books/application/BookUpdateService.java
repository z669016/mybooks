package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.*;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Class BookUpdateService
 * BookUpdateService provides write services for the book repository
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ToString
public class BookUpdateService implements BookManagementUpdatePort {
    private final BookPersistenceUpdatePort bookPersistenceUpdatePort;

    @Override
    public Author registerAuthor(String name, Map<SiteType, URL> sites) {
        log.info("registerAuthor({}, {})", name, sites);

        if (name == null || name.isBlank()) {
            log.warn(ServiceError.AUTHOR_NAME_REQUIRED.name());
            ServiceError.AUTHOR_NAME_REQUIRED.raise();
        }

        final Author author = bookPersistenceUpdatePort.registerAuthor(name, sites != null ? sites : Map.of());
        if (author == null)
            ServiceError.AUTHOR_NOT_REGISTERED.raise();

        return author;
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        log.info("forgetAuthor({})", authorId);

        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();

        bookPersistenceUpdatePort.forgetAuthor(authorId);
    }

    @Override
    public Author updateAuthor(AuthorId authorId, Instant version, String name) {
        log.info("updateAuthor({}, {}, {})", authorId, version, name);

        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();
        if (version == null)
            ServiceError.AUTHOR_VERSION_REQUIRED.raise();
        if (name == null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

        return bookPersistenceUpdatePort.updateAuthor(authorId, version, name);
    }

    @Override
    public Author setAuthorSite(AuthorId authorId, SiteType type, URL url) {
        log.info("setAuthorSite({}, {}, {})", authorId, type, url);

        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();
        if (type == null)
            ServiceError.AUTHOR_SITE_TYPE_REQUIRED.raise();
        if (url == null)
            ServiceError.AUTHOR_SITE_URL_INVALID.raise();

        final Author author = bookPersistenceUpdatePort.findAuthorById(authorId);
        if (author == null)
            ServiceError.AUTHOR_FOR_ID_NOT_FOUND.raise(authorId.toString());

        return bookPersistenceUpdatePort.setAuthorSite(authorId, type, url);
    }

    @Override
    public Book registerBook(BookId bookId, String title, Set<Author> authors, Set<MimeType> formats, Set<String> keywords) {
        log.info("registerBook({}, {}, {}, {})", bookId, title, authors, formats);

        if (bookId == null)
            ServiceError.BOOK_ID_REQUIRED.raise();
        if (title == null || title.isBlank())
            ServiceError.BOOK_TITLE_REQUIRED.raise();
        if (formats == null || formats.isEmpty())
            ServiceError.BOOK_FORMAT_REQUIRED.raise();
        if (keywords == null)
            ServiceError.BOOK_KEYWORDS_REQUIRED.raise();

        return bookPersistenceUpdatePort.registerBook(bookId, title, authors, formats, keywords);
    }
}
