package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.Author;
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

import java.util.Optional;
import java.util.Set;

@Validated
@RestController
public class AuthorController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BookManagementInquiryPort bookManagementInquiryPort;
    private final BookManagementUpdatePort bookManagementUpdatePort;

    public AuthorController(BookManagementInquiryPort bookManagementInquiryPort, BookManagementUpdatePort bookManagementUpdatePort) {
        this.bookManagementInquiryPort = bookManagementInquiryPort;
        this.bookManagementUpdatePort = bookManagementUpdatePort;
    }

    @GetMapping(path = "/authors", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<AuthorResponse> getAuthors() {
        try {
            return AuthorResponse.from(bookManagementInquiryPort.authors());
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage());
            logger.debug(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/author/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthorResponse getAuthorById(@PathVariable @ObjectIDConstraint String id) {
        try {
            final Optional<Author> author = bookManagementInquiryPort.authorById(AuthorId.withId(id));
            if (author.isPresent())
                    return AuthorResponse.from(author.get());
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage());
            logger.debug(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author with id " + id + " not found.");
    }

    @GetMapping(path = "/authors/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<AuthorResponse> getAuthorsByName(@PathVariable @NotBlank String name) {
        try {
            return AuthorResponse.from(bookManagementInquiryPort.authorsByName(name));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage());
            logger.debug(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @DeleteMapping(path = "/author/{id}")
    public void deleteAuthorById(@PathVariable  @ObjectIDConstraint String id) {
        try {
            bookManagementUpdatePort.forgetAuthor(AuthorId.withId(id));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage());
            logger.debug(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PostMapping(path = "/author",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse postAuthor(@RequestBody @Valid NewAuthorRequest author) {
        try {
            return AuthorResponse.from(bookManagementUpdatePort.registerAuthor(author.name(), author.sitesWithURLs()));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage());
            logger.debug(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PutMapping(path = "/author/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public AuthorResponse putAuthor(@PathVariable @ObjectIDConstraint String id, @Valid @RequestBody UpdateAuthorRequest author) {
        try {
            return AuthorResponse.from(bookManagementUpdatePort.updateAuthor(AuthorId.withId(id), author.versionAsInstant(), author.name()));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage());
            logger.debug(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }
}
