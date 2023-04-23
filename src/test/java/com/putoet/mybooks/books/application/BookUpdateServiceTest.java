package com.putoet.mybooks.books.application;

import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.application.port.out.persistence.BookUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BookUpdateServiceTest {
    private BookUpdatePort bookUpdatePort;
    private BookUpdateService bookUpdateService;

    @BeforeEach
    void setup() {
        bookUpdatePort = mock(BookUpdatePort.class);
        bookUpdateService = new BookUpdateService(bookUpdatePort);
    }

    @Test
    void registerAuthor() throws MalformedURLException {
        assertThrows(ServiceException.class, () -> bookUpdateService.registerAuthor(null, null));

        final String name = "Name, My";
        final URL url = new URL("https://nu.nl");
        final AuthorId id = AuthorId.withoutId();
        final Author author = new Author(id, Instant.now(), name, Map.of(SiteType.LINKEDIN, url));
        when(bookUpdatePort.registerAuthor(name, author.sites())).thenReturn(author);

        final Author created = bookUpdateService.registerAuthor(name, Map.of(SiteType.LINKEDIN, url));

        assertEquals(author, created);
    }

    @Test
    void registerAuthorFailed() {
        given(bookUpdatePort.findAuthorsByName(any())).willReturn(null);

        assertThrows(ServiceException.class, () -> bookUpdateService.registerAuthor("name", Map.of()));
    }

    @Test
    void registerAuthorError() {
        assertThrows(ServiceException.class, () -> bookUpdateService.registerAuthor(null, null));
    }

    @Test
    void forgetAuthor() {
        assertThrows(ServiceException.class, () -> bookUpdateService.forgetAuthor(null));

        final AuthorId id = AuthorId.withoutId();
        bookUpdateService.forgetAuthor(id);
        verify(bookUpdatePort).forgetAuthor(id);
    }

    @Test
    void updateAuthor() {
        assertThrows(ServiceException.class, () -> bookUpdateService.updateAuthor(null, null, null));
        assertThrows(ServiceException.class, () -> bookUpdateService.updateAuthor(AuthorId.withoutId(), null, null));
        assertThrows(ServiceException.class, () -> bookUpdateService.updateAuthor(AuthorId.withoutId(), Instant.now(), null));
        assertThrows(ServiceException.class, () -> bookUpdateService.updateAuthor(AuthorId.withoutId(), Instant.now(), " "));

        final AuthorId id = AuthorId.withoutId();
        final String name = "New, Name";
        final Instant version = Instant.now();
        final Author author = new Author(id, version, name, Map.of());
        when(bookUpdatePort.updateAuthor(id, version, name)).thenReturn(author);

        final Author updated = bookUpdateService.updateAuthor(id, version, name);
        verify(bookUpdatePort).updateAuthor(id, version, name);
        assertEquals(author, updated);
    }

    @Test
    void setAuthorSite() throws MalformedURLException {
        assertThrows(ServiceException.class, () -> bookUpdateService.setAuthorSite(null, null, null));
        assertThrows(ServiceException.class, () -> bookUpdateService.setAuthorSite(AuthorId.withoutId(), null, null));
        assertThrows(ServiceException.class, () -> bookUpdateService.setAuthorSite(AuthorId.withoutId(), SiteType.LINKEDIN, null));

        final AuthorId id = AuthorId.withoutId();
        final Author author = new Author(id, Instant.now(), "New, name", Map.of());
        final URL url = new URL("https://nu.nl");
        when(bookUpdatePort.findAuthorById(id)).thenReturn(author);
        when(bookUpdatePort.setAuthorSite(id, SiteType.LINKEDIN, url)).thenReturn(author);

        final Author updated = bookUpdateService.setAuthorSite(id, SiteType.LINKEDIN, url);
        verify(bookUpdatePort).findAuthorById(id);
        verify(bookUpdatePort).setAuthorSite(id, SiteType.LINKEDIN, url);

        assertNotNull(updated);
    }

    @Test
    void registerBook() {
        final Author author = new Author(AuthorId.withoutId(), Instant.now(), "New, name", Map.of());
        final BookId bookId = new BookId(BookId.BookIdScheme.ISBN, "978-1839211966");
        final String title = "Get Your Hands Dirty on Clean Architecture";
        final List<Author> authors = List.of(author);
        final List<MimeType> formats = List.of(MimeTypes.EPUB);
        final Book book = new Book(bookId, title, authors, Set.of(), new MimeTypes(formats));

        assertThrows(ServiceException.class, () -> bookUpdateService.registerBook(null, null, null, null, null));
        assertThrows(ServiceException.class, () -> bookUpdateService.registerBook(bookId, null, null, null, null));
        assertThrows(ServiceException.class, () -> bookUpdateService.registerBook(bookId, " ", null, null, null));
        assertThrows(ServiceException.class, () -> bookUpdateService.registerBook(bookId, title, null, null, null));
        assertThrows(ServiceException.class, () -> bookUpdateService.registerBook(bookId, title, authors, null, null));

        when(bookUpdatePort.registerBook(bookId, title, authors, new MimeTypes(formats), Set.of())).thenReturn(book);
        final Book created = bookUpdateService.registerBook(bookId, title, authors, formats, Set.of());

        verify(bookUpdatePort).registerBook(bookId, title, authors, new MimeTypes(formats), Set.of());
        assertNotNull(created);
        assertEquals(book, created);
    }
}