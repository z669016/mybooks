package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceQueryPort;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.AuthorTest;
import com.putoet.mybooks.books.domain.BookId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookInquiryServiceTest {
    private static final Author AUTHOR = AuthorTest.AUTHOR;
    private static final AuthorId AUTHOR_ID = AUTHOR.id();
    private static final String NAME = AUTHOR.name();

    @Mock
    private BookPersistenceQueryPort bookPersistenceQueryPort;

    @InjectMocks
    private BookInquiryService bookManagementInquiryPort;

    @Test
    void authorByIdFound() {
        given(bookPersistenceQueryPort.findAuthorById(AUTHOR_ID)).willReturn(AUTHOR);

        final var found = bookManagementInquiryPort.authorById(AUTHOR_ID);
        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findAuthorById(AUTHOR_ID),
                () -> assertEquals(Optional.of(AUTHOR), found)
        );
    }

    @Test
    void authorByIdNotFound() {
        given(bookPersistenceQueryPort.findAuthorById(any())).willReturn(null);

        final var found = bookManagementInquiryPort.authorById(AUTHOR_ID);
        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findAuthorById(AUTHOR_ID),
                () -> assertTrue(found.isEmpty())
        );
    }

    @Test
    void authorByIdError() {
        assertThrows(ServiceException.class, () -> bookManagementInquiryPort.authorById(null));
    }

    @Test
    void authorByNameFound() {
        given(bookPersistenceQueryPort.findAuthorsByName(NAME)).willReturn(Set.of(AUTHOR));

        final var found = bookManagementInquiryPort.authorsByName(NAME);
        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findAuthorsByName(NAME),
                () -> assertEquals(1, found.size()),
                () -> assertEquals(AUTHOR, found.stream().findFirst().orElseThrow())
        );
    }

    @Test
    void authorByNameNotFound() {
        given(bookPersistenceQueryPort.findAuthorsByName(any())).willReturn(Set.of());

        final var found = bookManagementInquiryPort.authorsByName(NAME);
        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findAuthorsByName(NAME),
                () -> assertTrue(found.isEmpty())
        );
    }

    @Test
    void authorsByName() {
        when(bookPersistenceQueryPort.findAuthorsByName("tim")).thenReturn(Set.of());
        when(bookPersistenceQueryPort.findAuthorsByName("tom")).thenReturn(Set.of(AuthorTest.AUTHOR));

        assertAll(
                () -> assertEquals(0, bookManagementInquiryPort.authorsByName("tim").size()),
                () -> verify(bookPersistenceQueryPort).findAuthorsByName("tim"),

                () -> assertEquals(1, bookManagementInquiryPort.authorsByName("tom").size()),
                () -> verify(bookPersistenceQueryPort).findAuthorsByName("tom"),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookManagementInquiryPort.authorsByName(null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementInquiryPort.authorsByName(" "))
        );
    }

    @Test
    void authorById() {
        when(bookPersistenceQueryPort.findAuthorById(any())).thenReturn(null);
        when(bookPersistenceQueryPort.findAuthorById(AuthorTest.AUTHOR.id())).thenReturn(AuthorTest.AUTHOR);
        final var author = bookManagementInquiryPort.authorById(AuthorTest.AUTHOR.id());

        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findAuthorById(AuthorTest.AUTHOR.id()),
                () -> assertEquals(Optional.of(AuthorTest.AUTHOR), author),
                () -> assertFalse(bookManagementInquiryPort.authorById(AuthorId.withoutId()).isPresent()),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookManagementInquiryPort.authorById(null))
        );
    }

    @Test
    void authors() {
        when(bookPersistenceQueryPort.findAuthors()).thenReturn(Set.of(AuthorTest.AUTHOR));
        final var authors = bookManagementInquiryPort.authors();

        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findAuthors(),
                () -> assertEquals(1, authors.size()),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookManagementInquiryPort.authorsByName(null))
        );
    }

    @Test
    void books() {
        when(bookPersistenceQueryPort.findBooks()).thenReturn(Set.of());
        final var books = bookManagementInquiryPort.books();

        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findBooks(),
                () -> assertEquals(0, books.size())
        );
    }

    @Test
    void bookByTitle() {
        when(bookPersistenceQueryPort.findBooksByTitle(any())).thenReturn(Set.of());
        final var books = bookManagementInquiryPort.booksByTitle("architecture");

        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findBooksByTitle("architecture"),
                () -> assertEquals(0, books.size()),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookManagementInquiryPort.booksByTitle(null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementInquiryPort.booksByTitle(" "))
        );
    }

    @Test
    void bookById() {
        when(bookPersistenceQueryPort.findBookById(any())).thenReturn(null);
        final var id = new BookId(BookId.BookIdSchema.UUID, UUID.randomUUID().toString());
        final var book = bookManagementInquiryPort.bookById(id);

        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findBookById(id),
                () -> assertTrue(book.isEmpty()),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookManagementInquiryPort.bookById(null))
        );
    }

    @Test
    void bookByAuthorName() {
        when(bookPersistenceQueryPort.findAuthorsByName("tom")).thenReturn(Set.of(AuthorTest.AUTHOR));
        when(bookPersistenceQueryPort.findBooksByAuthorId(AuthorTest.AUTHOR.id())).thenReturn(Set.of());
        final var books = bookManagementInquiryPort.booksByAuthorName("tom");

        assertAll(
                () -> verify(bookPersistenceQueryPort, times(1)).findAuthorsByName("tom"),
                () -> verify(bookPersistenceQueryPort, times(1)).findBooksByAuthorId(AuthorTest.AUTHOR.id()),
                () -> assertEquals(0, books.size()),

                () -> assertThrows(ServiceException.class, () -> bookManagementInquiryPort.booksByAuthorName(null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementInquiryPort.booksByAuthorName(" "))
        );
    }

    @Test
    void authorSiteTypes() {
        final var types = bookManagementInquiryPort.authorSiteTypes();
        assertEquals(7, types.size());
    }
}