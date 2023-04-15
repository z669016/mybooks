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
        assertTrue(found.isPresent());
        assertEquals(AUTHOR, found.get());
    }

    @Test
    void authorByIdNotFound() {
        given(bookQueryPort.findAuthorById(any())).willReturn(null);

        final var found = bookInquiryService.authorById(AUTHOR_ID);
        assertTrue(found.isEmpty());
    }

    @Test
    void authorByIdError() {
        assertThrows(ServiceException.class, () -> bookInquiryService.authorById(null));
    }

    @Test
    void authorByNameFound() {
        given(bookQueryPort.findAuthorsByName(NAME)).willReturn(List.of(AUTHOR));

        final var found = bookInquiryService.authorsByName(NAME);
        assertEquals(1, found.size());
        assertEquals(AUTHOR, found.get(0));
    }

    @Test
    void authorByNameNotFound() {
        given(bookQueryPort.findAuthorsByName(any())).willReturn(List.of());

        final var found = bookInquiryService.authorsByName(NAME);
        assertTrue(found.isEmpty());
    }

    @Test
    void authorByNameError() {
        assertThrows(ServiceException.class, () -> bookInquiryService.authorsByName(null));
        assertThrows(ServiceException.class, () -> bookInquiryService.authorsByName(""));
    }
    @Test
    void authorsByName() {
        assertThrows(ServiceException.class, () -> bookInquiryService.authorsByName(null));
        assertThrows(ServiceException.class, () -> bookInquiryService.authorsByName(" "));

        when(bookQueryPort.findAuthorsByName("tim")).thenReturn(List.of());
        when(bookQueryPort.findAuthorsByName("tom")).thenReturn(List.of(AuthorTest.AUTHOR));

        assertEquals(0, bookInquiryService.authorsByName("tim").size());
        verify(bookQueryPort).findAuthorsByName("tim");

        assertEquals(1, bookInquiryService.authorsByName("tom").size());
        verify(bookQueryPort).findAuthorsByName("tom");
    }

    @Test
    void authorById() {
        assertThrows(ServiceException.class, () -> bookInquiryService.authorById(null));

        when(bookQueryPort.findAuthorById(any())).thenReturn(null);
        when(bookQueryPort.findAuthorById(AuthorTest.AUTHOR.id())).thenReturn(AuthorTest.AUTHOR);

        final Optional<Author> author = bookInquiryService.authorById(AuthorTest.AUTHOR.id());
        assertTrue(author.isPresent());
        assertEquals(AuthorTest.AUTHOR, author.get());

        assertFalse(bookInquiryService.authorById(AuthorId.withoutId()).isPresent());
    }

    @Test
    void authors() {
        assertThrows(ServiceException.class, () -> bookInquiryService.authorsByName(null));

        when(bookQueryPort.findAuthors()).thenReturn(List.of(AuthorTest.AUTHOR, AuthorTest.AUTHOR));

        assertEquals(2, bookInquiryService.authors().size());
        verify(bookQueryPort).findAuthors();
    }

    @Test
    void books() {
        when(bookQueryPort.findBooks()).thenReturn(List.of());
        final List<Book> books = bookInquiryService.books();

        verify(bookQueryPort, times(1)).findBooks();
        assertEquals(0, books.size());
    }

    @Test
    void bookByTitle() {
        assertThrows(ServiceException.class, () -> bookInquiryService.booksByTitle(null));
        assertThrows(ServiceException.class, () -> bookInquiryService.booksByTitle(" "));

        when(bookQueryPort.findBooksByTitle(any())).thenReturn(List.of());
        final List<Book> books = bookInquiryService.booksByTitle("architecture");

        verify(bookQueryPort, times(1)).findBooksByTitle("architecture");
        assertEquals(0, books.size());
    }

    @Test
    void bookById() {
        assertThrows(ServiceException.class, () -> bookInquiryService.bookById(null));

        when(bookQueryPort.findBookById(any())).thenReturn(null);
        final BookId id = new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());
        final Optional<Book> book = bookInquiryService.bookById(id);

        verify(bookQueryPort, times(1)).findBookById(id);
        assertFalse(book.isPresent());
    }

    @Test
    void bookByAuthorName() {
        assertThrows(ServiceException.class, () -> bookInquiryService.booksByAuthorName(null));
        assertThrows(ServiceException.class, () -> bookInquiryService.booksByAuthorName(" "));

        when(bookQueryPort.findAuthorsByName("tom")).thenReturn(List.of(AuthorTest.AUTHOR));
        when(bookQueryPort.findBooksByAuthorId(AuthorTest.AUTHOR.id())).thenReturn(List.of());
        final List<Book> books = bookInquiryService.booksByAuthorName("tom");

        verify(bookQueryPort, times(1)).findAuthorsByName("tom");
        verify(bookQueryPort, times(1)).findBooksByAuthorId(AuthorTest.AUTHOR.id());
        assertEquals(0, books.size());
    }

    @Test
    void authorSiteTypes() {
        final List<String> types = bookInquiryService.authorSiteTypes();
        assertEquals(7, types.size());
    }
}