package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.*;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import org.springframework.stereotype.Service;

@Service("bookService")
public class BookService extends BookInquiryService implements
        RegisterAuthor, ForgetAuthor, UpdateAuthor, SetAuthorSite {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        super(bookRepository);
        this.bookRepository = bookRepository;
    }

    @Override
    public Author registerAuthor(RegisterAuthorCommand command) {
        if (command == null)
            ServiceError.AUTHOR_DETAILS_REQUIRED.raise();

        final Author author = bookRepository.createAuthor(command.name(), command.sites());
        if (author == null)
            ServiceError.AUTHOR_NOT_CREATED.raise();

        return author;
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        if (authorId == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();

        bookRepository.forgetAuthor(authorId);
    }

    @Override
    public Author updateAuthor(UpdateAuthorCommand command) {
        if (command == null)
            ServiceError.AUTHOR_DETAILS_REQUIRED.raise();

        return bookRepository.updateAuthor(command.id(), command.name());
    }

    @Override
    public Author setAuthorSite(SetAuthorSiteCommand command) {
        if (command == null)
            ServiceError.AUTHOR_SITE_DETAILS_REQUIRED.raise();

        final Author author = bookRepository.findAuthorById(command.authorId());
        if (author == null)
            ServiceError.AUTHOR_FOR_ID_NOT_FOUND.raise(command.authorId().toString());

        return bookRepository.setAuthorSite(command.authorId(), command.type(), command.url());
    }
}
