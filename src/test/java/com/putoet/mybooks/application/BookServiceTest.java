package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.ServiceException;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {
    private BookRepository repository;
    private BookService service;

    @BeforeEach
    void setup() {
        repository = mock(BookRepository.class);
        service = new BookService(repository);
    }

    @Test
    void registerAuthor() throws MalformedURLException {
        assertThrows(ServiceException.class, () -> service.registerAuthor(null, null));

        final String name = "Name, My";
        final URL url = new URL("https://nu.nl");
        final AuthorId id = AuthorId.withoutId();
        final Author author = new Author(id, name, Map.of(SiteType.LINKEDIN, url));
        when(repository.registerAuthor(name, author.sites())).thenReturn(author);

        final Author created = service.registerAuthor(name, Map.of(SiteType.LINKEDIN, url));

        assertEquals(author, created);
    }

    @Test
    void forgetAuthor() {
        assertThrows(ServiceException.class, () -> service.forgetAuthor(null));

        final AuthorId id = AuthorId.withoutId();
        service.forgetAuthor(id);
        verify(repository).forgetAuthor(id);
    }

    @Test
    void updateAuthor() {
        assertThrows(ServiceException.class, () -> service.updateAuthor(null, null));
        assertThrows(ServiceException.class, () -> service.updateAuthor(AuthorId.withoutId(), null));
        assertThrows(ServiceException.class, () -> service.updateAuthor(AuthorId.withoutId(), " "));

        final AuthorId id = AuthorId.withoutId();
        final String name = "New, Name";
        final Author author = new Author(id, name, Map.of());
        when(repository.updateAuthor(id, name)).thenReturn(author);

        final Author updated = service.updateAuthor(id, name);
        verify(repository).updateAuthor(id, name);
        assertEquals(author, updated);
    }

    @Test
    void setAuthorSite() throws MalformedURLException {
        assertThrows(ServiceException.class, () -> service.setAuthorSite(null, null, null));
        assertThrows(ServiceException.class, () -> service.setAuthorSite(AuthorId.withoutId(), null, null));
        assertThrows(ServiceException.class, () -> service.setAuthorSite(AuthorId.withoutId(), SiteType.LINKEDIN, null));

        final AuthorId id = AuthorId.withoutId();
        final Author author = new Author(id, "New, name", Map.of());
        when(repository.findAuthorById(id)).thenReturn(author);

        final URL url = new URL("https://nu.nl");

        service.setAuthorSite(id, SiteType.LINKEDIN, url);

        verify(repository).findAuthorById(id);
        verify(repository).setAuthorSite(id, SiteType.LINKEDIN, url);
    }
}