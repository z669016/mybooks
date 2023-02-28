package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.*;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("bookService")
public class BookService extends BookInquiryService implements RegisterAuthor, ForgetAuthor, UpdateAuthor {
    private final BookRepository authorRepository;

    public BookService(BookRepository authorRepository) {
        super(authorRepository);
        this.authorRepository = authorRepository;
    }

    @Override
    public Author registerAuthor(RegisterAuthorCommand command) {
        Objects.requireNonNull(command, "To register an author, provide the author details");

        final var author = new Author(AuthorId.withoutId(), command.name(), command.sites());
        final var registered = authorRepository.createAuthor(author);
        if (registered == null)
            throw new IllegalStateException("Unable to persist " + author);

        return registered;
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        Objects.requireNonNull(authorId, "To forget an author, provide the id   ");

//        final var registered = authorRepository.delete(authorId);
    }

    @Override
    public Author updateAuthor(Author author) {
        return null;
    }
}
