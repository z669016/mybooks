package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.out.BookInquiryRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import com.putoet.mybooks.domain.AuthorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookInquiryServiceTest {
    private BookInquiryRepository repository;
    private BookInquiryService service;

    @BeforeEach
    void setup() {
        repository = mock(BookInquiryRepository.class);
        service = new BookInquiryService(repository);
    }

    @Test
    void authorsByName() {
        assertThrows(NullPointerException.class, () -> service.authorsByName(null));

        when(repository.findAuthorsByName("tim")).thenReturn(List.of());
        when(repository.findAuthorsByName("tom")).thenReturn(List.of(AuthorTest.AUTHOR));

        assertEquals(0, service.authorsByName("tim").size());
        verify(repository).findAuthorsByName("tim");

        assertEquals(1, service.authorsByName("tom").size());
        verify(repository).findAuthorsByName("tom");
    }

    @Test
    void authorById() {
        assertThrows(NullPointerException.class, () -> service.authorById(null));

        when(repository.findAuthorById(any())).thenReturn(null);
        when(repository.findAuthorById(AuthorTest.AUTHOR.id())).thenReturn(AuthorTest.AUTHOR);

        final Optional<Author> author = service.authorById(AuthorTest.AUTHOR.id());
        assertTrue(author.isPresent());
        assertEquals(AuthorTest.AUTHOR, author.get());

        assertFalse(service.authorById(AuthorId.withoutId()).isPresent());
    }

    @Test
    void authors() {
        assertThrows(NullPointerException.class, () -> service.authorsByName(null));

        when(repository.findAuthors()).thenReturn(List.of(AuthorTest.AUTHOR, AuthorTest.AUTHOR));

        assertEquals(2, service.authors().size());
        verify(repository).findAuthors();
    }

    @Test
    void books() {
        // TODO
    }

    @Test
    void bookByTitle() {
        // TODO
    }

    @Test
    void bookById() {
        // TODO
    }

    @Test
    void bookByAuthorName() {
        // TODO
    }
}