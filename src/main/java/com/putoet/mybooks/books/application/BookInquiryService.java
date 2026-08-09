package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.*;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceQueryPort;
import com.putoet.mybooks.books.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class BookInquiryService contains all services (In ports) that only do read actions. This enables an implementation
 * on different media, like for instance a folder with EPUB books. By separating read-only and write-services
 * in enables several nice features, like a simple approach to load a database from EPUB books on a file system.
 */
@Service
@Transactional
public class BookInquiryService implements BookManagementInquiryPort {
    public static final Logger log = LoggerFactory.getLogger(BookInquiryService.class);

    private final BookPersistenceQueryPort bookPersistenceQueryPort;

    public BookInquiryService(BookPersistenceQueryPort bookPersistenceQueryPort) {
        this.bookPersistenceQueryPort = bookPersistenceQueryPort;
    }

    @Override
    public Set<Author> authorsByName(String name) {
        log.info("authorsByName({})", name);

        if (name== null || name.isBlank())
            throw ServiceError.AUTHOR_NAME_REQUIRED.exception();

        return bookPersistenceQueryPort.findAuthorsByName(name);
    }

    @Override
    public Optional<Author> authorById(AuthorId authorId) {
        log.info("authorById({})", authorId);
        if (authorId == null)
            throw ServiceError.AUTHOR_ID_REQUIRED.exception();

        return Optional.ofNullable(bookPersistenceQueryPort.findAuthorById(authorId));
    }

    @Override
    public Set<Author> authors() {
        log.info("authors()");

        return bookPersistenceQueryPort.findAuthors();
    }

    @Override
    public Set<Book> books() {
        log.info("books()");

        return bookPersistenceQueryPort.findBooks();
    }

    @Override
    public Set<Book> booksByTitle(String title) {
        log.info("booksByTitle({})", title);

        if (title== null || title.isBlank())
            throw ServiceError.BOOK_TITLE_REQUIRED.exception();

        return bookPersistenceQueryPort.findBooksByTitle(title);
    }

    @Override
    public Optional<Book> bookById(BookId bookId) {
        log.info("bookById({})", bookId);

        if (bookId == null)
            throw ServiceError.BOOK_ID_REQUIRED.exception();

        return Optional.ofNullable(bookPersistenceQueryPort.findBookById(bookId));
    }

    @Override
    public Set<Book> booksByAuthorName(String name) {
        log.info("booksByAuthorName({})", name);

        if (name == null || name.isBlank())
            throw ServiceError.AUTHOR_NAME_REQUIRED.exception();

        final Set<Author> authors = authorsByName(name);
        return authors.stream()
                .flatMap(author -> bookPersistenceQueryPort.findBooksByAuthorId(author.id()).stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> authorSiteTypes() {
        log.info("authorSiteTypes()");

        return Set.of(
                SiteType.HOMEPAGE_NAME,
                SiteType.FACEBOOK_NAME,
                SiteType.GITHUB_NAME,
                SiteType.LINKEDIN_NAME,
                SiteType.TWITTER_NAME,
                SiteType.INSTAGRAM_NAME,
                "Other"
                );
    }

    @Override
    public String toString() {
        return "BookInquiryService{" +
               "bookPersistenceQueryPort=" + bookPersistenceQueryPort +
               '}';
    }
}
