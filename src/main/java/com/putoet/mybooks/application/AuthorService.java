package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.AuthorById;
import com.putoet.mybooks.application.port.in.AuthorByName;
import com.putoet.mybooks.application.port.in.RegisterAuthor;
import com.putoet.mybooks.application.port.in.RegisterAuthorCommand;
import com.putoet.mybooks.application.port.out.AuthorRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service("authorService")
public class AuthorService implements AuthorByName, AuthorById, RegisterAuthor {
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public List<Author> authorByName(String name) {
        Objects.requireNonNull(name);
        if (name.isBlank())
            throw new IllegalArgumentException("authorByName must not be called with a blank name.");

        return authorRepository.findAuthorByName(name);
    }

    @Override
    public Optional<Author> authorById(AuthorId id) {
        Objects.requireNonNull(id);

        return Optional.ofNullable(authorRepository.findAuthorById(id));
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
