package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.application.port.in.ServiceError;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.application.security.event.AuthorCreatedSecurityEvent;
import com.putoet.mybooks.books.application.security.event.AuthorDeletedSecurityEvent;
import com.putoet.mybooks.books.application.security.event.BookCreatedSecurityEvent;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Class BookUpdateService
 * Service provides write services for the book repository
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ToString
public class BookUpdateService implements BookManagementUpdatePort {
    private final BookPersistenceUpdatePort bookPersistenceUpdatePort;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Author registerAuthor(String name, Map<SiteType, URL> sites) {
        log.info("registerAuthor({}, {})", name, sites);

        if (name == null || name.isBlank()) {
            log.warn(ServiceError.AUTHOR_NAME_REQUIRED.name());
            throw ServiceError.AUTHOR_NAME_REQUIRED.exception();
        }

        final Author author = bookPersistenceUpdatePort.registerAuthor(name, sites != null ? sites : Map.of());
        if (author == null)
            throw ServiceError.AUTHOR_NOT_REGISTERED.exception();

        applicationEventPublisher.publishEvent(new AuthorCreatedSecurityEvent(this, author.id()));
        return author;
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        log.info("forgetAuthor({})", authorId);

        if (authorId == null)
            throw ServiceError.AUTHOR_ID_REQUIRED.exception();

        bookPersistenceUpdatePort.forgetAuthor(authorId);
        applicationEventPublisher.publishEvent(new AuthorDeletedSecurityEvent(this, authorId));
    }

    @Override
    public Author updateAuthor(AuthorId authorId, Instant version, String name) {
        log.info("updateAuthor({}, {}, {})", authorId, version, name);

        if (authorId == null)
            throw ServiceError.AUTHOR_ID_REQUIRED.exception();
        if (version == null)
            throw ServiceError.AUTHOR_VERSION_REQUIRED.exception();
        if (name == null || name.isBlank())
            throw ServiceError.AUTHOR_NAME_REQUIRED.exception();

        return bookPersistenceUpdatePort.updateAuthor(authorId, version, name);
    }

    @Override
    public Author setAuthorSite(AuthorId authorId, SiteType type, URL url) {
        log.info("setAuthorSite({}, {}, {})", authorId, type, url);

        if (authorId == null)
            throw ServiceError.AUTHOR_ID_REQUIRED.exception();
        if (type == null)
            throw ServiceError.AUTHOR_SITE_TYPE_REQUIRED.exception();
        if (url == null)
            throw ServiceError.AUTHOR_SITE_URL_INVALID.exception();

        final Author author = bookPersistenceUpdatePort.findAuthorById(authorId);
        if (author == null)
            throw ServiceError.AUTHOR_FOR_ID_NOT_FOUND.exception(authorId.toString());

        return bookPersistenceUpdatePort.setAuthorSite(authorId, type, url);
    }

    @Override
    public Book registerBook(BookId bookId, String title, Set<Author> authors, Set<MimeType> formats, Set<String> keywords) {
        log.info("registerBook({}, {}, {}, {})", bookId, title, authors, formats);

        if (bookId == null)
            throw ServiceError.BOOK_ID_REQUIRED.exception();
        if (title == null || title.isBlank())
            throw ServiceError.BOOK_TITLE_REQUIRED.exception();
        if (formats == null || formats.isEmpty())
            throw ServiceError.BOOK_FORMAT_REQUIRED.exception();
        if (keywords == null)
            throw ServiceError.BOOK_KEYWORDS_REQUIRED.exception();

        final var book = bookPersistenceUpdatePort.registerBook(bookId, title, authors, formats, keywords);
        if (book == null)
            throw ServiceError.BOOK_NOT_REGISTERED.exception();

        applicationEventPublisher.publishEvent(new BookCreatedSecurityEvent(this, book.id()));
        return book;
    }
}
