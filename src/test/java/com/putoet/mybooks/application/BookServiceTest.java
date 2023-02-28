package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.RegisterAuthorCommand;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
        assertThrows(NullPointerException.class, () -> service.registerAuthor(null));

        final String name = "Putten, Margot van";
        final URL url = new URL("https://nl.linkedin.com/in/margot-van-putten-3a115615");

        final RegisterAuthorCommand command = RegisterAuthorCommand.withName(name)
                .withSite(SiteType.LINKEDIN, url)
                .build();

        final AuthorId id = AuthorId.withoutId();
        final Author author = new Author(id, name, Map.of(SiteType.LINKEDIN, new Site(SiteId.withoutId(), SiteType.LINKEDIN, url)));
        when(repository.createAuthor(any())).thenReturn(author);

        service.registerAuthor(command);

        final ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(repository).createAuthor(captor.capture());
        assertEquals(name, captor.getValue().name());
        assertNotNull(captor.getValue().sites().get(SiteType.LINKEDIN));
        assertEquals(url, captor.getValue().sites().get(SiteType.LINKEDIN).url());
    }
}