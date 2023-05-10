package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.Book;
import com.putoet.mybooks.books.domain.BookId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Validated
@RestController
public class BookController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BookManagementInquiryPort bookManagementInquiryPort;
    private final BookManagementUpdatePort bookManagementUpdatePort;

    private final SmartValidator validator;

    public BookController(BookManagementInquiryPort bookManagementInquiryPort, BookManagementUpdatePort bookManagementUpdatePort, SmartValidator validator) {
        this.bookManagementInquiryPort = bookManagementInquiryPort;
        this.bookManagementUpdatePort = bookManagementUpdatePort;
        this.validator = validator;
    }

    @GetMapping(path = "/books", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<BookResponse> getBooks() {
        try {
            return BookResponse.from(bookManagementInquiryPort.books());
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/books/author/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<BookResponse> getBooksByAuthorName(@PathVariable @NotBlank String name) {
        try {
            return BookResponse.from(bookManagementInquiryPort.booksByAuthorName(name));
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/books/{title}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<BookResponse> getBooksByTitle(@PathVariable @NotBlank String title) {
        try {
            return BookResponse.from(bookManagementInquiryPort.booksByTitle(title));
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/book/{schema}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookResponse getBookById(@PathVariable String schema, @PathVariable String id) throws MethodArgumentNotValidException {
        try {
            final ExistingBookRequest existingBookRequest = new ExistingBookRequest(schema, id);
            final BeanPropertyBindingResult result = new BeanPropertyBindingResult(existingBookRequest, "schema");
            validator.validate(existingBookRequest, result);
            if (result.hasErrors())
                throw new MethodArgumentNotValidException(new MethodParameter(this.getClass().getDeclaredMethod("getBookById", String.class, String.class), 0), result);
        } catch (NoSuchMethodException exc) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exc.getMessage());
        }

        try {
            final BookId bookId = new BookId(schema, id);
            final Optional<Book> book = bookManagementInquiryPort.bookById(bookId);
            if (book.isPresent())
                return BookResponse.from(book.get());
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "book with schema " + schema + " and id " + id + " not found");
    }

    @PostMapping(path = "/book",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BookResponse postBook(@RequestBody @Valid NewBookRequest book) {
        try {
            final BookId bookId = new BookId(book.schema(), book.id());
            final Set<Author> authors = new HashSet<>();
            for (BookRequestAuthor author : book.authors()) {
                if (author.isNewRequest()) {
                    final NewAuthorRequest newAuthorRequest = author.newAuthorRequest();
                    authors.add(bookManagementUpdatePort.registerAuthor(newAuthorRequest.name(), NewAuthorRequest.sitesWithURLs(newAuthorRequest.sites())));
                } else if (author.isExistingRequest()){
                    final ExistingAuthorRequest existingAuthorRequest = author.existingAuthorRequest();
                    authors.add(bookManagementInquiryPort.authorById(AuthorId.withId(existingAuthorRequest.id()))
                            .orElseThrow(() -> new IllegalArgumentException("author with id " + existingAuthorRequest.id() + " not found for book with id " + bookId))
                    );
                }
            }

            return BookResponse.from(bookManagementUpdatePort.registerBook(
                    bookId,
                    book.title(),
                    authors,
                    book.formatsAsMimeTypeList(),
                    book.keywords()
            ));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }
}
