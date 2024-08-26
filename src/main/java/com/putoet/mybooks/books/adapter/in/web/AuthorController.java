package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.validation.ObjectIDConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Validated
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthorController {
    private final BookManagementInquiryPort bookManagementInquiryPort;
    private final BookManagementUpdatePort bookManagementUpdatePort;

    @GetMapping(path = "/authors", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<AuthorResponse> getAuthors() {
        try {
            return AuthorResponse.from(bookManagementInquiryPort.authors());
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/author/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthorResponse getAuthorById(@PathVariable @ObjectIDConstraint String id) {
        try {
            final var author = bookManagementInquiryPort.authorById(AuthorId.withId(id));
            if (author.isPresent())
                return AuthorResponse.from(author.get());
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author with id " + id + " not found.");
    }

    @GetMapping(path = "/authors/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<AuthorResponse> getAuthorsByName(@PathVariable @NotBlank String name) {
        try {
            return AuthorResponse.from(bookManagementInquiryPort.authorsByName(name));
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @DeleteMapping(path = "/author/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthorById(@PathVariable @ObjectIDConstraint String id) {
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
    public AuthorResponse createAuthor(@RequestBody @Valid NewAuthorRequest author) {
        try {
            return AuthorResponse.from(bookManagementUpdatePort.registerAuthor(author.name(), author.sitesWithURLs()));
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PutMapping(path = "/author/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public AuthorResponse updateAuthor(@PathVariable @ObjectIDConstraint String id, @Valid @RequestBody UpdateAuthorRequest author) {
        try {
            return AuthorResponse.from(bookManagementUpdatePort.updateAuthor(AuthorId.withId(id), author.versionAsInstant(), author.name()));
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }
}
