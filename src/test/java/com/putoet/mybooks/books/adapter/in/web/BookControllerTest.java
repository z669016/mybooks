package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookManagementInquiryPort bookManagementInquiryPort;

    @Mock
    private BookManagementUpdatePort bookManagementUpdatePort;

    @Mock
    private SmartValidator smartValidator;

    @InjectMocks
    private BookController bookController;

    private final Author author = new Author(AuthorId.withoutId(), "Schrijver, Jaap de");

    private final Set<String> formats = Set.of(MimeTypes.PDF.toString(), MimeTypes.EPUB.toString());
    private final Book book = new Book(new BookId(BookId.BookIdSchema.ISBN, "978-1-83921-196-6"),
            "Get Your Hands Dirty on Clean Architecture",
            Set.of(author),
            Set.of("architecture", "rest"),
            BookResponse.toDomain(formats)
    );

    @Test
    void getBooks() {
        final var books = bookController.getBooks();
        assertAll(
                () -> assertEquals(0, books.size()),
                () -> verify(bookManagementInquiryPort, times(1)).books()
        );
    }

    @Test
    void getBooksFailed() {
        when(bookManagementInquiryPort.books()).thenThrow(new RuntimeException("FAIL"));
        try {
            bookController.getBooks();
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertAll(
                    () -> verify(bookManagementInquiryPort, times(1)).books(),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode())
            );
        }
    }

    @Test
    void getBooksByAuthorName() {
        bookController.getBooksByAuthorName(author.name());
        verify(bookManagementInquiryPort, times(1)).booksByAuthorName(author.name());
    }

    @Test
    void getBooksByAuthorNameFailed() {
        when(bookManagementInquiryPort.booksByAuthorName(author.name())).thenThrow(new RuntimeException("FAIL"));
        try {
            bookController.getBooksByAuthorName(author.name());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertAll(
                    () -> verify(bookManagementInquiryPort, times(1)).booksByAuthorName(author.name()),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode())
            );
        }
    }

    @Test
    void getBooksByTitle() {
        bookController.getBooksByTitle(book.title());
        verify(bookManagementInquiryPort, times(1)).booksByTitle(book.title());
    }

    @Test
    void getBooksByTitleFailed() {
        when(bookManagementInquiryPort.booksByTitle(book.title())).thenThrow(new RuntimeException("FAIL"));
        try {
            bookController.getBooksByTitle(book.title());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertAll(
                    () -> verify(bookManagementInquiryPort, times(1)).booksByTitle(book.title()),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode())
            );
        }
    }

    @Test
    void getBookById() throws MethodArgumentNotValidException {
        when(bookManagementInquiryPort.bookById(book.id())).thenReturn(Optional.of(book));
        bookController.getBookById(book.id().schema().name(), book.id().id());
        verify(bookManagementInquiryPort, times(1)).bookById(book.id());
    }

    @Test
    void getBookByIdFailed() throws MethodArgumentNotValidException {
        try {
            bookController.getBookById("BLA", "123");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void getBookByIdNotFound() throws MethodArgumentNotValidException {
        when(bookManagementInquiryPort.bookById(book.id())).thenReturn(Optional.empty());
        try {
            bookController.getBookById(book.id().schema().name(), book.id().id());
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.NOT_FOUND, exc.getStatusCode());
        }
    }

    @Test
    void postBook() {
        final var firstAuthor = new BookRequestAuthor(author.id().uuid().toString(), null, null);
        final var secondAuthor = new BookRequestAuthor(null, "Author, Second", Map.of());

        final NewBookRequest newBookRequest = new NewBookRequest(
                book.id().schema().name(),
                book.id().id(),
                book.title(),
                Set.of(firstAuthor, secondAuthor),
                book.keywords(),
                Set.of(MimeTypes.EPUB.toString())
        );

        final var createdAuthor = new Author(AuthorId.withoutId(), secondAuthor.name());

        when(bookManagementInquiryPort.authorById(author.id())).thenReturn(Optional.of(author));
        when(bookManagementUpdatePort.registerAuthor(secondAuthor.name(), Map.of())).thenReturn(createdAuthor);

        when(bookManagementUpdatePort.registerBook(
                eq(book.id()),
                eq(book.title()),
                eq(Set.of(author, createdAuthor)),
                eq(BookResponse.toDomain(newBookRequest.formats())),
                eq(book.keywords())
        )).thenReturn(book);

        final var createdBook = bookController.postBook(newBookRequest);
        assertAll(
                () -> assertNotNull(createdBook),
                () -> verify(bookManagementInquiryPort, times(1)).authorById(author.id()),
                () -> verify(bookManagementUpdatePort, times(1)).registerAuthor(secondAuthor.name(), Map.of())
        );
    }
}