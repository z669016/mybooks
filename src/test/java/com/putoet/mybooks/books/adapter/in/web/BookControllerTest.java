package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.BookInquiryService;
import com.putoet.mybooks.books.application.BookUpdateService;
import com.putoet.mybooks.books.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
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

    private final Author author = new Author(AuthorId.withoutId(), Instant.now(), "Schrijver, Jaap de", Map.of());
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
        final List<BookResponse> books = bookController.getBooksByAuthorName(author.name());
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
        final List<BookResponse> books = bookController.getBooksByTitle(book.title());
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
    }
}