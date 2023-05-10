package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    void setup() {
        bookPersistenceUpdatePort = mock(BookPersistenceUpdatePort.class);
        bookManagementUpdatePort = new BookUpdateService(bookPersistenceUpdatePort);
    }

    @Test
    void registerAuthor() throws MalformedURLException {

        final String name = "Name, My";
        final URL url = new URL("https://nu.nl");
        final AuthorId id = AuthorId.withoutId();
        final Author author = new Author(id, Instant.now(), name, Map.of(SiteType.LINKEDIN, url));
        when(bookPersistenceUpdatePort.registerAuthor(name, author.sites())).thenReturn(author);

        final Author created = bookManagementUpdatePort.registerAuthor(name, Map.of(SiteType.LINKEDIN, url));
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

        final AuthorId id = AuthorId.withoutId();
        bookManagementUpdatePort.forgetAuthor(id);
        verify(bookPersistenceUpdatePort).forgetAuthor(id);
    }

    @Test
    void updateAuthor() {
        final AuthorId id = AuthorId.withoutId();
        final String name = "New, Name";
        final Instant version = Instant.now();
        final Author author = new Author(id, version, name, Map.of());
        when(bookPersistenceUpdatePort.updateAuthor(id, version, name)).thenReturn(author);

        final Author updated = bookManagementUpdatePort.updateAuthor(id, version, name);
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
        final AuthorId id = AuthorId.withoutId();
        final Author author = new Author(id, Instant.now(), "New, name", Map.of());
        final URL url = new URL("https://nu.nl");
        when(bookPersistenceUpdatePort.findAuthorById(id)).thenReturn(author);
        when(bookPersistenceUpdatePort.setAuthorSite(id, SiteType.LINKEDIN, url)).thenReturn(author);

        final Author updated = bookManagementUpdatePort.setAuthorSite(id, SiteType.LINKEDIN, url);
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
        final Author author = new Author(AuthorId.withoutId(), Instant.now(), "New, name", Map.of());
        final BookId bookId = new BookId(BookId.BookIdScheme.ISBN, "978-1839211966");
        final String title = "Get Your Hands Dirty on Clean Architecture";
        final Set<Author> authors = Set.of(author);
        final Set<MimeType> formats = Set.of(MimeTypes.EPUB);
        final Book book = new Book(bookId, title, authors, Set.of(), new MimeTypes(formats));

        when(bookPersistenceUpdatePort.registerBook(bookId, title, authors, new MimeTypes(formats), Set.of())).thenReturn(book);
        final Book created = bookManagementUpdatePort.registerBook(bookId, title, authors, formats, Set.of());

        assertAll(
                () -> verify(bookPersistenceUpdatePort).registerBook(bookId, title, authors, new MimeTypes(formats), Set.of()),
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