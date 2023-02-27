package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.*;
import com.putoet.mybooks.application.port.out.BookReadOnlyRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import com.putoet.mybooks.domain.Book;
import com.putoet.mybooks.domain.BookId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service("authorService")
public class BookInquiryService implements Books, BooksByTitle, BookById, Authors, AuthorsByName, AuthorById, BooksByAuthorName {
    private final BookReadOnlyRepository bookRepository;

    public BookInquiryService(BookReadOnlyRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Author> authorsByName(String name) {
        Objects.requireNonNull(name, "Author name must be provided for name based search");
        if (name.isBlank())
            throw new IllegalArgumentException("authorByName must not be called with a blank name.");

        return bookRepository.findAuthorsByName(name);
    }

    @Override
    public Optional<Author> authorById(AuthorId id) {
        Objects.requireNonNull(id, "Author id must be provided for an id based search");

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
    public List<Book> bookByTitle(String title) {
        Objects.requireNonNull(title, "(Part of the) book title must be provided for a title based search");
        if (title.isBlank())
            throw new IllegalArgumentException("booksByTitle must not be called with a blank title.");

        return bookRepository.findBooksByTitle(title);
    }

    @Override
    public Optional<Book> bookById(BookId bookId) {
        Objects.requireNonNull(bookId, "Book id must be provided for an id based search");

        return Optional.ofNullable(bookRepository.findBookById(bookId));
    }

    @Override
    public List<Book> bookByAuthorName(String name) {
        Objects.requireNonNull(name, "Author name must be provided for author name based book search");
        if (name.isBlank())
            throw new IllegalArgumentException("booksByAuthorName must not be called with a blank name.");

        final List<Author> authors = authorsByName(name);

        return authors.stream()
                .flatMap(author -> bookRepository.findBooksByAuthorId(author.id()).stream())
                .toList();
    }
}
