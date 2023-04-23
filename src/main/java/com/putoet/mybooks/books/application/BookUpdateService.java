package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.*;
import com.putoet.mybooks.books.application.port.out.persistence.BookUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class BookUpdateService
 * BookUpdateService provides write services for the book repository
 */
@Service("bookUpdateService")
public class BookUpdateService implements
        RegisterAuthor, ForgetAuthor, UpdateAuthor, SetAuthorSite, RegisterBook {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BookUpdatePort bookUpdatePort;

    public BookUpdateService(BookUpdatePort bookUpdatePort) {
        this.bookUpdatePort = bookUpdatePort;

        logger.info("BookService({})", bookUpdatePort);
    }

    @Override
    public Author registerAuthor(String name, Map<SiteType, URL> sites) {
        logger.info("registerAuthor({}, {})", name, sites);

        if (name == null || name.isBlank()) {
            logger.warn(ServiceError.AUTHOR_NAME_REQUIRED.name());
            ServiceError.AUTHOR_NAME_REQUIRED.raise();
        }

        final Author author = bookUpdatePort.registerAuthor(name, sites != null ? sites : Map.of());
        if (author == null)
            ServiceError.AUTHOR_NOT_REGISTERED.raise();

        return author;
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        logger.info("forgetAuthor({})", authorId);

        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();

        bookUpdatePort.forgetAuthor(authorId);
    }

    @Override
    public Author updateAuthor(AuthorId authorId, Instant version, String name) {
        logger.info("updateAuthor({}, {}, {})", authorId, version, name);

        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();
        if (version == null)
            ServiceError.AUTHOR_VERSION_REQUIRED.raise();
        if (name == null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

        return bookUpdatePort.updateAuthor(authorId, version, name);
    }

    @Override
    public Author setAuthorSite(AuthorId authorId, SiteType type, URL url) {
        logger.info("setAuthorSite({}, {}, {})", authorId, type, url);

        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();
        if (type == null)
            ServiceError.AUTHOR_SITE_TYPE_REQUIRED.raise();
        if (url == null)
            ServiceError.AUTHOR_SITE_URL_INVALID.raise();

        final Author author = bookUpdatePort.findAuthorById(authorId);
        if (author == null)
            ServiceError.AUTHOR_FOR_ID_NOT_FOUND.raise(authorId.toString());

        return bookUpdatePort.setAuthorSite(authorId, type, url);
    }

    @Override
    public Book registerBook(BookId bookId, String title, List<Author> authors, List<MimeType> formats, Set<String> keywords) {
        logger.info("registerBook({}, {}, {}, {})", bookId, title, authors, formats);

        if (bookId == null)
            ServiceError.BOOK_ID_REQUIRED.raise();
        if (title == null || title.isBlank())
            ServiceError.BOOK_TITLE_REQUIRED.raise();
        if (formats == null || formats.isEmpty())
            ServiceError.BOOK_FORMAT_REQUIRED.raise();
        if (keywords == null)
            ServiceError.BOOK_KEYWORDS_REQUIRED.raise();

        return bookUpdatePort.registerBook(bookId, title, authors, new MimeTypes(formats), keywords);
    }
}
