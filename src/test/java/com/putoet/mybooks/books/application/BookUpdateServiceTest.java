package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BookUpdateServiceTest {
    private BookPersistenceUpdatePort bookPersistenceUpdatePort;
    private BookManagementUpdatePort bookManagementUpdatePort;
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        bookPersistenceUpdatePort = mock(BookPersistenceUpdatePort.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        bookManagementUpdatePort = new BookUpdateService(bookPersistenceUpdatePort, applicationEventPublisher);
    }

    @Test
    void registerAuthor() throws MalformedURLException {
        final var name = "Name, My";
        final var url = new URL("https://nu.nl");
        final var id = AuthorId.withoutId();
        final var author = new Author(id, Instant.now(), name, Map.of(SiteType.LINKEDIN, url));
        when(bookPersistenceUpdatePort.registerAuthor(name, author.sites())).thenReturn(author);

        final var created = bookManagementUpdatePort.registerAuthor(name, Map.of(SiteType.LINKEDIN, url));
        assertAll(
                () -> assertEquals(author, created),

                // error condition
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.registerAuthor(null, null))
        );
    }

    @Test
    void registerAuthorFailed() {
        given(bookPersistenceUpdatePort.findAuthorsByName(any())).willReturn(null);

        assertThrows(ServiceException.class, () -> bookManagementUpdatePort.registerAuthor("name", Map.of()));
    }

    @Test
    void registerAuthorError() {
        assertThrows(ServiceException.class, () -> bookManagementUpdatePort.registerAuthor(null, null));
    }

    @Test
    void forgetAuthor() {
        assertThrows(ServiceException.class, () -> bookManagementUpdatePort.forgetAuthor(null));

        final var id = AuthorId.withoutId();
        bookManagementUpdatePort.forgetAuthor(id);
        verify(bookPersistenceUpdatePort).forgetAuthor(id);
    }

    @Test
    void updateAuthor() {
        final var id = AuthorId.withoutId();
        final var name = "New, Name";
        final var version = Instant.now();
        final var author = new Author(id, version, name, Map.of());
        when(bookPersistenceUpdatePort.updateAuthor(id, version, name)).thenReturn(author);

        final var updated = bookManagementUpdatePort.updateAuthor(id, version, name);
        assertAll(
                () -> verify(bookPersistenceUpdatePort).updateAuthor(id, version, name),
                () -> assertEquals(author, updated),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.updateAuthor(null, null, null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.updateAuthor(AuthorId.withoutId(), null, null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.updateAuthor(AuthorId.withoutId(), Instant.now(), null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.updateAuthor(AuthorId.withoutId(), Instant.now(), " "))
        );
    }

    @Test
    void setAuthorSite() throws MalformedURLException {
        final var id = AuthorId.withoutId();
        final var author = new Author(id, Instant.now(), "New, name", Map.of());
        final var url = new URL("https://nu.nl");
        when(bookPersistenceUpdatePort.findAuthorById(id)).thenReturn(author);
        when(bookPersistenceUpdatePort.setAuthorSite(id, SiteType.LINKEDIN, url)).thenReturn(author);

        final var updated = bookManagementUpdatePort.setAuthorSite(id, SiteType.LINKEDIN, url);
        assertAll(
                () -> verify(bookPersistenceUpdatePort).findAuthorById(id),
                () -> verify(bookPersistenceUpdatePort).setAuthorSite(id, SiteType.LINKEDIN, url),
                () -> assertNotNull(updated),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.setAuthorSite(null, null, null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.setAuthorSite(AuthorId.withoutId(), null, null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.setAuthorSite(AuthorId.withoutId(), SiteType.LINKEDIN, null))
        );
    }

    @Test
    void registerBook() {
        final var author = new Author(AuthorId.withoutId(), Instant.now(), "New, name", Map.of());
        final var bookId = new BookId(BookId.BookIdSchema.ISBN, "978-1839211966");
        final var title = "Get Your Hands Dirty on Clean Architecture";
        final var authors = Set.of(author);
        final var formats = Set.of(MimeTypes.EPUB);
        final var book = new Book(bookId, title, authors, Set.of(), formats);

        when(bookPersistenceUpdatePort.registerBook(bookId, title, authors, formats, Set.of())).thenReturn(book);
        final var created = bookManagementUpdatePort.registerBook(bookId, title, authors, formats, Set.of());

        assertAll(
                () -> verify(bookPersistenceUpdatePort).registerBook(bookId, title, authors, formats, Set.of()),
                () -> assertNotNull(created),
                () -> assertEquals(book, created),

                // error conditions
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.registerBook(null, null, null, null, null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.registerBook(bookId, null, null, null, null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.registerBook(bookId, " ", null, null, null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.registerBook(bookId, title, null, null, null)),
                () -> assertThrows(ServiceException.class, () -> bookManagementUpdatePort.registerBook(bookId, title, authors, null, null))
        );
    }
}