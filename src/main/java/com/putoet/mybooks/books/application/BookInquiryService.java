package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.*;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceQueryPort;
import com.putoet.mybooks.books.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class BookInquiryService contains all services (In ports) that only do read actions. This enables an implementation
 * on different media, like for instance a folder with EPUB books. By separating read-only and write-services
 * in enables several nice features, like a simple approach to load a database from EPUB books on a file system.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ToString
public class BookInquiryService implements BookManagementInquiryPort {

    private final BookPersistenceQueryPort bookPersistenceQueryPort;

    @Override
    public Set<Author> authorsByName(String name) {
        log.info("authorsByName({})", name);

        if (name== null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

        return bookPersistenceQueryPort.findAuthorsByName(name);
    }

    @Override
    public Optional<Author> authorById(AuthorId authorId) {
        log.info("authorById({})", authorId);
        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();

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
            ServiceError.BOOK_TITLE_REQUIRED.raise();

        return bookPersistenceQueryPort.findBooksByTitle(title);
    }

    @Override
    public Optional<Book> bookById(BookId bookId) {
        log.info("bookById({})", bookId);

        if (bookId == null)
            ServiceError.BOOK_ID_REQUIRED.raise();

        return Optional.ofNullable(bookPersistenceQueryPort.findBookById(bookId));
    }

    @Override
    public Set<Book> booksByAuthorName(String name) {
        log.info("booksByAuthorName({})", name);

        if (name == null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

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
}
