package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.*;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("authorService")
public class BookService extends BookInquiryService implements RegisterAuthor {
    private final BookRepository authorRepository;

    public BookService(BookRepository authorRepository) {
        super(authorRepository);
        this.authorRepository = authorRepository;
    }

    @Override
    public Author registerAuthor(RegisterAuthorCommand command) {
        Objects.requireNonNull(command);

        final var author = new Author(AuthorId.withoutId(), command.name(), command.sites());
        final var registered = authorRepository.persist(author);
        if (registered == null)
            throw new IllegalStateException("Unable to persist " + author);

        return registered;
    }
}
