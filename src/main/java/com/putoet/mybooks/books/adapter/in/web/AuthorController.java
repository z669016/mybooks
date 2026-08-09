package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.validation.ObjectIDConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Validated
@RestController
public class AuthorController {
    public static final Logger log = LoggerFactory.getLogger(AuthorController.class);

    private final BookManagementInquiryPort bookManagementInquiryPort;
    private final BookManagementUpdatePort bookManagementUpdatePort;

    public AuthorController(BookManagementInquiryPort bookManagementInquiryPort,  BookManagementUpdatePort bookManagementUpdatePort) {
        this.bookManagementInquiryPort = bookManagementInquiryPort;
        this.bookManagementUpdatePort = bookManagementUpdatePort;
        log.debug("AuthorController('{}', '{}')", bookManagementInquiryPort, bookManagementUpdatePort);
    }

    @GetMapping(path = "/authors", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<AuthorResponse> getAuthors() {
        log.debug("getAuthors()");
        try {
            final var authors = AuthorResponse.from(bookManagementInquiryPort.authors());
            log.debug("get authors returns: {}", authors);
            return authors;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/author/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthorResponse getAuthorById(@PathVariable @ObjectIDConstraint String id) {
        log.debug("getAuthorById('{}')", id);
        try {
            final var author = bookManagementInquiryPort.authorById(AuthorId.withId(id));
            if (author.isPresent()) {
                log.debug("get author by id returns: {}", author.get());
                return AuthorResponse.from(author.get());
            }
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author with id " + id + " not found.");
    }

    @GetMapping(path = "/authors/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<AuthorResponse> getAuthorsByName(@PathVariable @NotBlank String name) {
        log.debug("getAuthorsByName('{}')", name);
        try {
            final var author = AuthorResponse.from(bookManagementInquiryPort.authorsByName(name));
            log.debug("get authors by name returns: {}", author);
            return author;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @DeleteMapping(path = "/author/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthorById(@PathVariable @ObjectIDConstraint String id) {
        log.debug("deleteAuthorById('{}')", id);
        try {
            bookManagementUpdatePort.forgetAuthor(AuthorId.withId(id));
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PostMapping(path = "/author",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse createAuthor(@RequestBody @Valid NewAuthorRequest request) {
        log.debug("createAuthor('{}')", request);
        try {
            final var author = AuthorResponse.from(bookManagementUpdatePort.registerAuthor(request.name(), request.sitesWithURLs()));
            log.debug("create author returns: {})", author);
            return author;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PutMapping(path = "/author/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public AuthorResponse updateAuthor(@PathVariable @ObjectIDConstraint String id, @Valid @RequestBody UpdateAuthorRequest request) {
        log.debug("updateAuthor('{}', '{}')", id, request);
        try {
            final var author = AuthorResponse.from(bookManagementUpdatePort.updateAuthor(AuthorId.withId(id), request.versionAsInstant(), request.name()));
            log.debug("update author returns: {}", author);
            return author;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }
}
