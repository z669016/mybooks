package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.BookInquiryService;
import com.putoet.mybooks.books.application.BookUpdateService;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.Book;
import com.putoet.mybooks.books.domain.BookId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class BookController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BookInquiryService bookInquiryService;
    private final BookUpdateService bookUpdateService;


    public BookController(BookInquiryService bookInquiryService, BookUpdateService bookUpdateService) {
        this.bookInquiryService = bookInquiryService;
        this.bookUpdateService = bookUpdateService;
    }

    @GetMapping(path = "/books", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BookResponse> getBooks() {
        try {
            return BookResponse.from(bookInquiryService.books());
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/books/author/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BookResponse> getBooksByAuthorName(@PathVariable String name) {
        try {
            return BookResponse.from(bookInquiryService.booksByAuthorName(name));
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/books/{title}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BookResponse> getBooksByTitle(@PathVariable String title) {
        try {
            return BookResponse.from(bookInquiryService.booksByTitle(title));
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }


    @GetMapping(path = "/book/{schema}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookResponse getBookById(@PathVariable String schema, @PathVariable String id) {
        try {
            final BookId bookId = new BookId(schema, id);
            final Optional<Book> book = bookInquiryService.bookById(bookId);
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
    public BookResponse postBook(@RequestBody BookResponse book) {
        try {
            final BookId bookId = new BookId(book.schema(), book.id());
            final List<Author> authors = new ArrayList<>();
            for (AuthorResponse author : book.authors()) {
                if (author.id() == null) {
                    authors.add(bookUpdateService.registerAuthor(author.name(), NewAuthorRequest.sitesWithURLs(author.sites())));
                } else {
                    authors.add(bookInquiryService.authorById(AuthorId.withId(author.id()))
                            .orElseThrow(() -> new IllegalArgumentException("author with id " + author.id() + " not found for book with id " + bookId))
                    );
                }
            }

            return BookResponse.from(bookUpdateService.registerBook(
                    bookId,
                    book.title(),
                    authors,
                    BookResponse.toDomain(book.formats()),
                    book.keywords()
            ));
        } catch (RuntimeException exc) {
            logger.warn(exc.getMessage(), exc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }
}
