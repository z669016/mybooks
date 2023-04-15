package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.*;
import com.putoet.mybooks.application.port.out.BookInquiryRepository;
import com.putoet.mybooks.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Class BookInquiryService
 * BookInquiryService contains all services (In ports) that only do read actions. This enables an implementation
 * on different media, like for instance a folder with EPUB books. By separating read-only and write-services
 * in enables several nice features, like a simple approach to load a databse from EPUB books on a file system.
 */
@Service("bookInquiryService")
public class BookInquiryService implements
        Books, BooksByTitle, BookById, Authors, AuthorsByName, AuthorById, BooksByAuthorName,
        AuthorSiteTypes {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BookInquiryRepository bookRepository;

    public BookInquiryService(BookInquiryRepository bookRepository) {
        this.bookRepository = bookRepository;

        logger.info("BookInquiryService({})", bookRepository);
    }

    @Override
    public List<Author> authorsByName(String name) {
        logger.info("authorsByName({})", name);

        if (name== null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

        return bookRepository.findAuthorsByName(name);
    }

    @Override
    public Optional<Author> authorById(AuthorId authorId) {
        logger.info("authorById({})", authorId);
        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();

        return Optional.ofNullable(bookRepository.findAuthorById(authorId));
    }

    @Override
    public List<Author> authors() {
        logger.info("authors()");

        return bookRepository.findAuthors();
    }

    @Override
    public List<Book> books() {
        logger.info("books()");

        return bookRepository.findBooks();
    }

    @Override
    public List<Book> booksByTitle(String title) {
        logger.info("booksByTitle({})", title);

        if (title== null || title.isBlank())
            ServiceError.BOOK_TITLE_REQUIRED.raise();

        return bookRepository.findBooksByTitle(title);
    }

    @Override
    public Optional<Book> bookById(BookId bookId) {
        logger.info("bookById({})", bookId);

        if (bookId == null)
            ServiceError.BOOK_ID_REQUIRED.raise();

        return Optional.ofNullable(bookRepository.findBookById(bookId));
    }

    @Override
    public List<Book> booksByAuthorName(String name) {
        logger.info("booksByAuthorName({})", name);

        if (name == null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

        final List<Author> authors = authorsByName(name);
        return authors.stream()
                .flatMap(author -> bookRepository.findBooksByAuthorId(author.id()).stream())
                .toList();
    }

    @Override
    public List<String> authorSiteTypes() {
        logger.info("authorSiteTypes()");

        return List.of(
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
