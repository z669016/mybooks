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

class BookServiceTest {
    private BookUpdatePort bookUpdatePort;
    private BookService bookService;

    @BeforeEach
    void setup() {
        bookUpdatePort = mock(BookUpdatePort.class);
        bookService = new BookService(bookUpdatePort);
    }

    @Test
    void registerAuthor() throws MalformedURLException {
        assertThrows(ServiceException.class, () -> bookService.registerAuthor(null, null));

        final String name = "Name, My";
        final URL url = new URL("https://nu.nl");
        final AuthorId id = AuthorId.withoutId();
        final Author author = new Author(id, Instant.now(), name, Map.of(SiteType.LINKEDIN, url));
        when(bookUpdatePort.registerAuthor(name, author.sites())).thenReturn(author);

        final Author created = bookService.registerAuthor(name, Map.of(SiteType.LINKEDIN, url));

        assertEquals(author, created);
    }

    @Test
    void registerAuthorFailed() {
        given(bookUpdatePort.findAuthorsByName(any())).willReturn(null);

        assertThrows(ServiceException.class, () -> bookService.registerAuthor("name", Map.of()));
    }

    @Test
    void registerAuthorError() {
        assertThrows(ServiceException.class, () -> bookService.registerAuthor(null, null));
    }

    @Test
    void forgetAuthor() {
        assertThrows(ServiceException.class, () -> bookService.forgetAuthor(null));

        final AuthorId id = AuthorId.withoutId();
        bookService.forgetAuthor(id);
        verify(bookUpdatePort).forgetAuthor(id);
    }

    @Test
    void updateAuthor() {
        assertThrows(ServiceException.class, () -> bookService.updateAuthor(null, null));
        assertThrows(ServiceException.class, () -> bookService.updateAuthor(AuthorId.withoutId(), null));
        assertThrows(ServiceException.class, () -> bookService.updateAuthor(AuthorId.withoutId(), " "));

        final AuthorId id = AuthorId.withoutId();
        final String name = "New, Name";
        final Author author = new Author(id, Instant.now(), name, Map.of());
        when(bookUpdatePort.updateAuthor(id, name)).thenReturn(author);

        final Author updated = bookService.updateAuthor(id, name);
        verify(bookUpdatePort).updateAuthor(id, name);
        assertEquals(author, updated);
    }

    @Test
    void setAuthorSite() throws MalformedURLException {
        assertThrows(ServiceException.class, () -> bookService.setAuthorSite(null, null, null));
        assertThrows(ServiceException.class, () -> bookService.setAuthorSite(AuthorId.withoutId(), null, null));
        assertThrows(ServiceException.class, () -> bookService.setAuthorSite(AuthorId.withoutId(), SiteType.LINKEDIN, null));

        final AuthorId id = AuthorId.withoutId();
        final Author author = new Author(id, Instant.now(), "New, name", Map.of());
        final URL url = new URL("https://nu.nl");
        when(bookUpdatePort.findAuthorById(id)).thenReturn(author);
        when(bookUpdatePort.setAuthorSite(id, SiteType.LINKEDIN, url)).thenReturn(author);

        final Author updated = bookService.setAuthorSite(id, SiteType.LINKEDIN, url);
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

        assertThrows(ServiceException.class, () -> bookService.registerBook(null, null, null, null, null));
        assertThrows(ServiceException.class, () -> bookService.registerBook(bookId, null, null, null, null));
        assertThrows(ServiceException.class, () -> bookService.registerBook(bookId, " ", null, null, null));
        assertThrows(ServiceException.class, () -> bookService.registerBook(bookId, title, null, null, null));
        assertThrows(ServiceException.class, () -> bookService.registerBook(bookId, title, authors, null, null));

        when(bookUpdatePort.registerBook(bookId, title, authors, new MimeTypes(formats), Set.of())).thenReturn(book);
        final Book created = bookService.registerBook(bookId, title, authors, formats, Set.of());

        verify(bookUpdatePort).registerBook(bookId, title, authors, new MimeTypes(formats), Set.of());
        assertNotNull(created);
        assertEquals(book, created);
    }
}