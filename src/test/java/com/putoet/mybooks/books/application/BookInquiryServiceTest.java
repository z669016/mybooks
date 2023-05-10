package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.application.port.out.persistence.BookQueryPort;
import com.putoet.mybooks.books.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BookInquiryServiceTest {
    private static final Author AUTHOR = AuthorTest.AUTHOR;
    private static final AuthorId AUTHOR_ID = AUTHOR.id();
    private static final String NAME = AUTHOR.name();

    private BookQueryPort bookQueryPort;
    private BookInquiryService bookInquiryService;

    @BeforeEach
    void setup() {
        bookQueryPort = mock(BookQueryPort.class);
        bookInquiryService = new BookInquiryService(bookQueryPort);
    }

    @Test
    void authorByIdFound() {
        given(bookQueryPort.findAuthorById(AUTHOR_ID)).willReturn(AUTHOR);

        final var found = bookInquiryService.authorById(AUTHOR_ID);
        assertAll(
                () -> verify(bookQueryPort,times(1)).findAuthorById(AUTHOR_ID),
                () -> assertEquals(Optional.of(AUTHOR),found)
        );
    }

    @Test
    void authorByIdNotFound() {
        given(bookQueryPort.findAuthorById(any())).willReturn(null);

        final var found = bookInquiryService.authorById(AUTHOR_ID);
        assertAll(
                () -> verify(bookQueryPort, times(1)).findAuthorById(AUTHOR_ID),
                () -> assertTrue(found.isEmpty())
        );
    }

    @Test
    void authorByIdError() {
        assertThrows(ServiceException.class, () -> bookInquiryService.authorById(null));
    }

    @Test
    void authorByNameFound() {
        given(bookQueryPort.findAuthorsByName(NAME)).willReturn(List.of(AUTHOR));

        final var found = bookInquiryService.authorsByName(NAME);
        assertAll(
                () -> verify(bookQueryPort, times(1)).findAuthorsByName(NAME),
                () -> assertEquals(1, found.size()),
                () -> assertEquals(AUTHOR, found.get(0))
        );
    }

    @Test
    void authorByNameNotFound() {
        given(bookQueryPort.findAuthorsByName(any())).willReturn(List.of());

        final var found = bookInquiryService.authorsByName(NAME);
        assertAll(
                () -> verify(bookQueryPort, times(1)).findAuthorsByName(NAME),
                () -> assertTrue(found.isEmpty())
        );
    }

    @Test
    void authorsByName() {
        when(bookQueryPort.findAuthorsByName("tim")).thenReturn(List.of());
        when(bookQueryPort.findAuthorsByName("tom")).thenReturn(List.of(AuthorTest.AUTHOR));

        assertAll(
                () -> assertEquals(0, bookInquiryService.authorsByName("tim").size()),
                () -> verify(bookQueryPort).findAuthorsByName("tim"),

                () -> assertEquals(1, bookInquiryService.authorsByName("tom").size()),
                () -> verify(bookQueryPort).findAuthorsByName("tom"),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookInquiryService.authorsByName(null)),
                () -> assertThrows(ServiceException.class, () -> bookInquiryService.authorsByName(" "))
        );
    }

    @Test
    void authorById() {
        when(bookQueryPort.findAuthorById(any())).thenReturn(null);
        when(bookQueryPort.findAuthorById(AuthorTest.AUTHOR.id())).thenReturn(AuthorTest.AUTHOR);
        final Optional<Author> author = bookInquiryService.authorById(AuthorTest.AUTHOR.id());

        assertAll(
                () -> verify(bookQueryPort, times(1)).findAuthorById(AuthorTest.AUTHOR.id()),
                () -> assertEquals(Optional.of(AuthorTest.AUTHOR), author),
                () -> assertFalse(bookInquiryService.authorById(AuthorId.withoutId()).isPresent()),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookInquiryService.authorById(null))
        );
    }

    @Test
    void authors() {
        when(bookQueryPort.findAuthors()).thenReturn(List.of(AuthorTest.AUTHOR, AuthorTest.AUTHOR));
        final List<Author> authors = bookInquiryService.authors();

        assertAll(
                () -> verify(bookQueryPort, times(1)).findAuthors(),
                () -> assertEquals(2, authors.size()),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookInquiryService.authorsByName(null))
        );
    }

    @Test
    void books() {
        when(bookQueryPort.findBooks()).thenReturn(List.of());
        final List<Book> books = bookInquiryService.books();

        assertAll(
                () -> verify(bookQueryPort, times(1)).findBooks(),
                () -> assertEquals(0, books.size())
        );
    }

    @Test
    void bookByTitle() {
        when(bookQueryPort.findBooksByTitle(any())).thenReturn(List.of());
        final List<Book> books = bookInquiryService.booksByTitle("architecture");

        assertAll(
                () -> verify(bookQueryPort, times(1)).findBooksByTitle("architecture"),
                () -> assertEquals(0, books.size()),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookInquiryService.booksByTitle(null)),
                () -> assertThrows(ServiceException.class, () -> bookInquiryService.booksByTitle(" "))
        );
    }

    @Test
    void bookById() {
        when(bookQueryPort.findBookById(any())).thenReturn(null);
        final BookId id = new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());
        final Optional<Book> book = bookInquiryService.bookById(id);

        assertAll(
                () -> verify(bookQueryPort, times(1)).findBookById(id),
                () -> assertTrue(book.isEmpty()),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookInquiryService.bookById(null))
        );
    }

    @Test
    void bookByAuthorName() {
        when(bookQueryPort.findAuthorsByName("tom")).thenReturn(List.of(AuthorTest.AUTHOR));
        when(bookQueryPort.findBooksByAuthorId(AuthorTest.AUTHOR.id())).thenReturn(List.of());
        final List<Book> books = bookInquiryService.booksByAuthorName("tom");

        assertAll(
                () -> verify(bookQueryPort, times(1)).findAuthorsByName("tom"),
                () -> verify(bookQueryPort, times(1)).findBooksByAuthorId(AuthorTest.AUTHOR.id()),
                () -> assertEquals(0, books.size()),

                () -> assertThrows(ServiceException.class, () -> bookInquiryService.booksByAuthorName(null)),
                () -> assertThrows(ServiceException.class, () -> bookInquiryService.booksByAuthorName(" "))
        );
    }

    @Test
    void authorSiteTypes() {
        final List<String> types = bookInquiryService.authorSiteTypes();
        assertEquals(7, types.size());
    }
}