package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.BookInquiryService;
import com.putoet.mybooks.books.application.BookUpdateService;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
public class AuthorController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BookInquiryService bookInquiryService;
    private final BookUpdateService bookUpdateService;

    public AuthorController(BookInquiryService bookInquiryService, BookUpdateService bookUpdateService) {
        this.bookInquiryService = bookInquiryService;
        this.bookUpdateService = bookUpdateService;
    }

    @GetMapping("/authors")
    public List<AuthorResponse> getAuthors() {
        try {
            return AuthorResponse.from(bookInquiryService.authors());
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping("/author/{id}")
    public AuthorResponse getAuthorById(@PathVariable String id) {
        try {
            if (id == null)
                throw new IllegalArgumentException("id is null");

            final Optional<Author> author = bookInquiryService.authorById(AuthorId.withId(id));
            if (author.isPresent())
                    return AuthorResponse.from(author.get());
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author with id " + id + " not found.");
    }

    @GetMapping("/authors/{name}")
    public List<AuthorResponse> getAuthorsByName(@PathVariable String name) {
        try {
            if (name == null)
                throw new IllegalArgumentException("name is null");

            return AuthorResponse.from(bookInquiryService.authorsByName(name));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @DeleteMapping("/author/{id}")
    public void deleteAuthorById(@PathVariable String id) {
        try {
            if (id == null)
                throw new IllegalArgumentException("id is null");

            bookUpdateService.forgetAuthor(AuthorId.withId(id));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PostMapping("/author")
    public AuthorResponse postAuthor(@RequestBody AuthorResponse author) {
        try {
            if (author.id() != null)
                throw new IllegalArgumentException("author id must be empty when creating a new author, instead the value is '" + author.id() + "'");
            if (author.version() != null)
                throw new IllegalArgumentException("author version must be empty when creating a new author, instead the value is '" + author.version() + "'");

            return AuthorResponse.from(bookUpdateService.registerAuthor(author.name(), AuthorResponse.toDomain(author.sites())));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PutMapping("/author/{id}")
    public AuthorResponse putAuthor(@PathVariable String id, @RequestBody AuthorResponse author) {
        try {
            if (id == null || !id.equals(author.id()))
                throw new IllegalArgumentException("author id is not set or path parameter differs from body parameter");
            if (author.version() == null)
                throw new IllegalArgumentException("author version is not set");
            final Instant version = Instant.parse(author.version());

            return AuthorResponse.from(bookUpdateService.updateAuthor(AuthorId.withId(id), version, author.name()));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }
}
