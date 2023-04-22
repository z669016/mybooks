package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.BookService;
import com.putoet.mybooks.books.domain.AuthorId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
public class AuthorController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BookService bookService;

    public AuthorController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/authors")
    public List<Author> getAuthors() {
        return Author.fromDomain(bookService.authors());
    }

    @GetMapping("/author/{id}")
    public Author getAuthorById(@PathVariable String id) {
        try {
            if (id == null)
                throw new IllegalArgumentException("id is null");

            return bookService.authorById(AuthorId.withId(id))
                    .map(Author::fromDomain)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author with id " + id + " not found."));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping("/authors/{name}")
    public List<Author> getAuthorsByName(@PathVariable String name) {
        try {
            if (name == null)
                throw new IllegalArgumentException("name is null");

            return Author.fromDomain(bookService.authorsByName(name));
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

            bookService.forgetAuthor(AuthorId.withId(id));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PostMapping("/author")
    public Author postAuthor(@RequestBody Author author) {
        try {
            if (author.id() != null)
                throw new IllegalArgumentException("author id must be empty when creating a new author, instead the value is '" + author.id() + "'");
            if (author.version() != null)
                throw new IllegalArgumentException("author version must be empty when creating a new author, instead the value is '" + author.version() + "'");

            return Author.fromDomain(bookService.registerAuthor(author.name(),Author.toDomain(author.sites())));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PutMapping("/author/{id}")
    public Author putAuthor(@PathVariable String id, @RequestBody Author author) {
        try {
            if (id == null || !id.equals(author.id()))
                throw new IllegalArgumentException("author id is not set or path parameter differs from body parameter");
            if (author.version() == null)
                throw new IllegalArgumentException("author version is not set");
            final Instant version = Instant.parse(author.version());

            return Author.fromDomain(bookService.updateAuthor(AuthorId.withId(id), version, author.name()));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }
}
