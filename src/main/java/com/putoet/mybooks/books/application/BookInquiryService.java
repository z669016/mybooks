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
        log.debug("BookInquiryService('{}", bookPersistenceQueryPort);
    }

    @Override
    public Set<Author> authorsByName(String name) {
        log.debug("authorsByName('{}')", name);

        if (name== null || name.isBlank())
            throw ServiceError.AUTHOR_NAME_REQUIRED.exception();

        final var authors = bookPersistenceQueryPort.findAuthorsByName(name);
        log.debug("authors by name returns: {}", authors);
        return authors;
    }

    @Override
    public Optional<Author> authorById(AuthorId authorId) {
        log.debug("authorById('{}')", authorId);
        if (authorId == null)
            throw ServiceError.AUTHOR_ID_REQUIRED.exception();

        final var author = Optional.ofNullable(bookPersistenceQueryPort.findAuthorById(authorId));
        log.debug("author by id returns: {}", author);
        return author;
    }

    @Override
    public Set<Author> authors() {
        log.debug("authors()");

        final var authors = bookPersistenceQueryPort.findAuthors();
        log.debug("authors returns: {}", authors);
        return authors;
    }

    @Override
    public Set<Book> books() {
        log.debug("books()");

        final var books = bookPersistenceQueryPort.findBooks();
        log.debug("books returns: {}", books);
        return books;
    }

    @Override
    public Set<Book> booksByTitle(String title) {
        log.debug("booksByTitle('{}')", title);

        if (title== null || title.isBlank())
            throw ServiceError.BOOK_TITLE_REQUIRED.exception();

        final var books = bookPersistenceQueryPort.findBooksByTitle(title);
        log.debug("books by title returns: {}", books);
        return books;
    }

    @Override
    public Optional<Book> bookById(BookId bookId) {
        log.debug("bookById('{}')", bookId);

        if (bookId == null)
            throw ServiceError.BOOK_ID_REQUIRED.exception();

        final var book = Optional.ofNullable(bookPersistenceQueryPort.findBookById(bookId));
        log.debug("book by id returns: {}", book);
        return book;
    }

    @Override
    public Set<Book> booksByAuthorName(String name) {
        log.debug("booksByAuthorName('{}')", name);

        if (name == null || name.isBlank())
            throw ServiceError.AUTHOR_NAME_REQUIRED.exception();

        final Set<Author> authors = authorsByName(name);
        final var books = authors.stream()
                .flatMap(author -> bookPersistenceQueryPort.findBooksByAuthorId(author.id()).stream())
                .collect(Collectors.toSet());
        log.debug("books by author name returns: {}", books);
        return books;
    }

    @Override
    public Set<String> authorSiteTypes() {
        log.debug("authorSiteTypes()");

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
