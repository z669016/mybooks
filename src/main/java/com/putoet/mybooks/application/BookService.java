package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.*;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.*;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.Map;

@Service("bookService")
public class BookService extends BookInquiryService implements
        RegisterAuthor, ForgetAuthor, UpdateAuthor, SetAuthorSite, RegisterBook {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        super(bookRepository);
        this.bookRepository = bookRepository;
    }

    @Override
    public Author registerAuthor(String name, Map<SiteType, URL> sites) {
        if (name == null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

        final Author author = bookRepository.registerAuthor(name, sites != null ? sites : Map.of());
        if (author == null)
            ServiceError.AUTHOR_NOT_REGISTERED.raise();

        return author;
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();

        bookRepository.forgetAuthor(authorId);
    }

    @Override
    public Author updateAuthor(AuthorId authorId, String name) {
        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();
        if (name == null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();

        return bookRepository.updateAuthor(authorId, name);
    }

    @Override
    public Author setAuthorSite(AuthorId authorId, SiteType type, URL url) {
        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();
        if (type == null)
            ServiceError.AUTHOR_SITE_TYPE_REQUIRED.raise();
        if (url == null)
            ServiceError.AUTHOR_SITE_URL_INVALID.raise();

        final Author author = bookRepository.findAuthorById(authorId);
        if (author == null)
            ServiceError.AUTHOR_FOR_ID_NOT_FOUND.raise(authorId.toString());

        return bookRepository.setAuthorSite(authorId, type, url);
    }

    @Override
    public Book registerBook(BookId bookId, String title, List<Author> authors, String description, List<FormatType> formats) {
        if (bookId == null)
            ServiceError.BOOK_ID_REQUIRED.raise();
        if (title == null || title.isBlank())
            ServiceError.BOOK_TITLE_REQUIRED.raise();
        if (authors == null || authors.isEmpty())
            ServiceError.BOOK_AUTHORS_REQUIRED.raise();
        if (description == null || description.isBlank())
            ServiceError.BOOK_DESCRIPTION_REQUIRED.raise();
        if (formats == null || formats.isEmpty())
            ServiceError.BOOK_FORMAT_REQUIRED.raise();

        return bookRepository.registerBook(bookId, title, authors, description, formats);
    }
}
