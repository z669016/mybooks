package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.BookInquiryService;
import com.putoet.mybooks.books.application.BookUpdateService;
import com.putoet.mybooks.books.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookControllerTest {
    private BookInquiryService bookInquiryService;
    private BookUpdateService bookUpdateService;
    private BookController bookController;

    private final Author author = new Author(AuthorId.withoutId(), "Schrijver, Jaap de");

    private final List<String> formats = List.of(MimeTypes.PDF.toString(), MimeTypes.EPUB.toString());
    private final Book book = new Book(new BookId(BookId.BookIdScheme.ISBN, "978-1-83921-196-6"),
            "Get Your Hands Dirty on Clean Architecture",
            List.of(author),
            Set.of("architecture", "rest"),
            new MimeTypes(BookResponse.toDomain(formats))
    );

    @BeforeEach
    void setup() {
        bookInquiryService = mock(BookInquiryService.class);
        bookUpdateService = mock(BookUpdateService.class);
        bookController = new BookController(bookInquiryService, bookUpdateService);
    }

    @Test
    void getBooks() {
        final List<BookResponse> books = bookController.getBooks();
        assertEquals(0, books.size());
        verify(bookInquiryService, times(1)).books();
    }

    @Test
    void getBooksFailed() {
        when(bookInquiryService.books()).thenThrow(new RuntimeException("FAIL"));
        try {
            bookController.getBooks();
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            verify(bookInquiryService, times(1)).books();
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void getBooksByAuthorName() {
        bookController.getBooksByAuthorName(author.name());
        verify(bookInquiryService, times(1)).booksByAuthorName(author.name());
    }

    @Test
    void getBooksByAuthorNameFailed() {
        when(bookInquiryService.booksByAuthorName(author.name())).thenThrow(new RuntimeException("FAIL"));
        try {
            bookController.getBooksByAuthorName(author.name());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            verify(bookInquiryService, times(1)).booksByAuthorName(author.name());
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void getBooksByTitle() {
        bookController.getBooksByTitle(book.title());
        verify(bookInquiryService, times(1)).booksByTitle(book.title());
    }

    @Test
    void getBooksByTitleFailed() {
        when(bookInquiryService.booksByTitle(book.title())).thenThrow(new RuntimeException("FAIL"));
        try {
            bookController.getBooksByTitle(book.title());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            verify(bookInquiryService, times(1)).booksByTitle(book.title());
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void getBookById() {
        when(bookInquiryService.bookById(book.id())).thenReturn(Optional.of(book));
        bookController.getBookById(book.id().schema().name(), book.id().id());
        verify(bookInquiryService, times(1)).bookById(book.id());
    }

    @Test
    void getBookByIdFailed() {
        try {
            bookController.getBookById("BLA", "123");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void getBookByIdNotFound() {
        when(bookInquiryService.bookById(book.id())).thenReturn(Optional.empty());
        try {
            bookController.getBookById(book.id().schema().name(), book.id().id());
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.NOT_FOUND, exc.getStatusCode());
        }
    }

    @Test
    void postBook() {
        final AuthorResponse firstAuthor = new AuthorResponse(
                author.id().uuid().toString(),
                author.version().toString(),
                author.name(),
                Map.of()
        );

        final AuthorResponse secondAuthor = new AuthorResponse(
                null,
                null,
                "Author, Second",
                Map.of()
        );

        final BookResponse bookResponse = new BookResponse(
                book.id().schema().name(),
                book.id().id(),
                book.title(),
                List.of(firstAuthor, secondAuthor),
                book.keywords(),
                List.of(MimeTypes.EPUB.toString())
        );

        final Author createdAuthor = new Author(AuthorId.withoutId(), secondAuthor.name());

        when(bookInquiryService.authorById(author.id())).thenReturn(Optional.of(author));
        when(bookUpdateService.registerAuthor(secondAuthor.name(), Map.of())).thenReturn(createdAuthor);

        when(bookUpdateService.registerBook(
                eq(book.id()),
                eq(book.title()),
                eq(List.of(author, createdAuthor)),
                eq(BookResponse.toDomain(bookResponse.formats())),
                eq(book.keywords())
        )).thenReturn(book);

        final BookResponse createdBook = bookController.postBook(bookResponse);
        assertNotNull(createdBook);

        verify(bookInquiryService, times(1)).authorById(author.id());
        verify(bookUpdateService, times(1)).registerAuthor(secondAuthor.name(), Map.of());
    }
}