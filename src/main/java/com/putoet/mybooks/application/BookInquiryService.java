package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.*;
import com.putoet.mybooks.application.port.out.BookInquiryRepository;
import com.putoet.mybooks.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("bookInquiryService")
public class BookInquiryService implements
        Books, BooksByTitle, BookById, Authors, AuthorsByName, AuthorById, BooksByAuthorName,
        AuthorSiteTypes {
    private final BookInquiryRepository bookRepository;

    public BookInquiryService(BookInquiryRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Author> authorsByName(String name) {
        if (name== null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

        return bookRepository.findAuthorsByName(name);
    }

    @Override
    public Optional<Author> authorById(AuthorId id) {
        if (id == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();

        return Optional.ofNullable(bookRepository.findAuthorById(id));
    }

    @Override
    public List<Author> authors() {
        return bookRepository.findAuthors();
    }

    @Override
    public List<Book> books() {
        return bookRepository.findBooks();
    }

    @Override
    public List<Book> booksByTitle(String title) {
        if (title== null || title.isBlank())
            ServiceError.BOOK_TITLE_REQUIRED.raise();

        return bookRepository.findBooksByTitle(title);
    }

    @Override
    public Optional<Book> bookById(BookId bookId) {
        if (bookId == null)
            ServiceError.BOOK_ID_REQUIRED.raise();

        return Optional.ofNullable(bookRepository.findBookById(bookId));
    }

    @Override
    public List<Book> booksByAuthorName(String name) {
        if (name == null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

        final List<Author> authors = authorsByName(name);
        return authors.stream()
                .flatMap(author -> bookRepository.findBooksByAuthorId(author.id()).stream())
                .toList();
    }

    @Override
    public List<String> authorSiteTypes() {
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
