package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
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

import java.util.HashSet;
import java.util.Set;

@Validated
@RestController
public class BookController {
    public static final Logger log = LoggerFactory.getLogger(BookController.class);

    private final BookManagementInquiryPort bookManagementInquiryPort;
    private final BookManagementUpdatePort bookManagementUpdatePort;
    private final SmartValidator validator;

    public BookController(BookManagementInquiryPort bookManagementInquiryPort,
                          BookManagementUpdatePort bookManagementUpdatePort,
                          SmartValidator validator) {
        this.bookManagementInquiryPort = bookManagementInquiryPort;
        this.bookManagementUpdatePort = bookManagementUpdatePort;
        this.validator = validator;
        log.debug("BookController('{}','{}','{}')", bookManagementInquiryPort, bookManagementUpdatePort, validator);
    }

    @GetMapping(
            path = "/api/v{version}/books",
            version = "1.0",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Set<BookResponse> getBooks() {
        log.debug("getBooks()");
        try {
            final var books = BookResponse.from(bookManagementInquiryPort.books());
            log.debug("get books returns: {}", books);
            return books;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(
            path = "/api/v{version}/books/author/{name}",
            version = "1.0",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Set<BookResponse> getBooksByAuthorName(@PathVariable @NotBlank String name) {
        log.debug("getBooksByAuthorName('{}')", name);
        try {
            final var books = BookResponse.from(bookManagementInquiryPort.booksByAuthorName(name));
            log.debug("books by auth returns: {}", books);
            return books;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(
            path = "/api/v{version}/books/{title}",
            version = "1.0",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Set<BookResponse> getBooksByTitle(@PathVariable @NotBlank String title) {
        log.debug("getBooksByTitle('{}')", title);
        try {
            final var books = BookResponse.from(bookManagementInquiryPort.booksByTitle(title));
            log.debug("books by title returns: {}", books);
            return books;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(
            path = "/api/v{version}/book/{schema}/{id}",
            version = "1.0",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BookResponse getBookById(@PathVariable String schema, @PathVariable String id) throws MethodArgumentNotValidException {
        log.debug("getBookById('{}', '{}')", schema, id);
        try {
            final var existingBookRequest = new ExistingBookRequest(schema, id);
            final var result = new BeanPropertyBindingResult(existingBookRequest, "schema");
            validator.validate(existingBookRequest, result);
            if (result.hasErrors())
                throw new MethodArgumentNotValidException(new MethodParameter(this.getClass().getDeclaredMethod("getBookById", String.class, String.class), 0), result);
        } catch (NoSuchMethodException exc) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exc.getMessage());
        }

        try {
            final var bookId = new BookId(schema, id);
            final var book = bookManagementInquiryPort.bookById(bookId);
            if (book.isPresent()) {
                log.debug("get book by id returns:{}", book.get());
                return BookResponse.from(book.get());
            }
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "book with schema " + schema + " and id " + id + " not found");
    }

    @PostMapping(
            path = "/api/v{version}/book",
            version = "1.0",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@RequestBody @Valid NewBookRequest request) {
        log.debug("createBook('{}')", request);
        try {
            final var bookId = new BookId(request.schema(), request.id());
            final var authors = new HashSet<Author>();
            for (BookRequestAuthor author : request.authors()) {
                if (author.isNewRequest()) {
                    final var newAuthorRequest = author.newAuthorRequest();
                    authors.add(bookManagementUpdatePort.registerAuthor(newAuthorRequest.name(), NewAuthorRequest.sitesWithURLs(newAuthorRequest.sites())));
                } else if (author.isExistingRequest()) {
                    final var existingAuthorRequest = author.existingAuthorRequest();
                    authors.add(bookManagementInquiryPort.authorById(AuthorId.withId(existingAuthorRequest.id()))
                            .orElseThrow(() -> new IllegalArgumentException("author with id " + existingAuthorRequest.id() + " not found for book with id " + bookId))
                    );
                }
            }

            final var book =  BookResponse.from(bookManagementUpdatePort.registerBook(
                    bookId,
                    request.title(),
                    authors,
                    request.formatsAsMimeTypeList(),
                    request.keywords()
            ));
            log.debug("create book returns: {}", book);
            return book;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }
}
