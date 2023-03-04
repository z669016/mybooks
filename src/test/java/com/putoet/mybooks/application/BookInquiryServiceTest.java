package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.ServiceError;
import com.putoet.mybooks.application.port.in.ServiceException;
import com.putoet.mybooks.application.port.out.BookInquiryRepository;
import com.putoet.mybooks.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        assertThrows(ServiceException.class, () -> service.authorsByName(null));
        assertThrows(ServiceException.class, () -> service.authorsByName(" "));

        when(repository.findAuthorsByName("tim")).thenReturn(List.of());
        when(repository.findAuthorsByName("tom")).thenReturn(List.of(AuthorTest.AUTHOR));

        assertEquals(0, service.authorsByName("tim").size());
        verify(repository).findAuthorsByName("tim");

        assertEquals(1, service.authorsByName("tom").size());
        verify(repository).findAuthorsByName("tom");
    }

    @Test
    void authorById() {
        assertThrows(ServiceException.class, () -> service.authorById(null));

        when(repository.findAuthorById(any())).thenReturn(null);
        when(repository.findAuthorById(AuthorTest.AUTHOR.id())).thenReturn(AuthorTest.AUTHOR);

        final Optional<Author> author = service.authorById(AuthorTest.AUTHOR.id());
        assertTrue(author.isPresent());
        assertEquals(AuthorTest.AUTHOR, author.get());

        assertFalse(service.authorById(AuthorId.withoutId()).isPresent());
    }

    @Test
    void authors() {
        assertThrows(ServiceException.class, () -> service.authorsByName(null));

        when(repository.findAuthors()).thenReturn(List.of(AuthorTest.AUTHOR, AuthorTest.AUTHOR));

        assertEquals(2, service.authors().size());
        verify(repository).findAuthors();
    }

    @Test
    void books() {
        when(repository.findBooks()).thenReturn(List.of());
        final List<Book> books = service.books();

        verify(repository, times(1)).findBooks();
        assertEquals(0, books.size());
    }

    @Test
    void bookByTitle() {
        assertThrows(ServiceException.class, () -> service.booksByTitle(null));
        assertThrows(ServiceException.class, () -> service.booksByTitle(" "));

        when(repository.findBooksByTitle(any())).thenReturn(List.of());
        final List<Book> books = service.booksByTitle("architecture");

        verify(repository, times(1)).findBooksByTitle("architecture");
        assertEquals(0, books.size());
    }

    @Test
    void bookById() {
        assertThrows(ServiceException.class, () -> service.bookById(null));

        when(repository.findBookById(any())).thenReturn(null);
        final BookId id = new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());
        final Optional<Book> book = service.bookById(id);

        verify(repository, times(1)).findBookById(id);
        assertFalse(book.isPresent());
    }

    @Test
    void bookByAuthorName() {
        assertThrows(ServiceException.class, () -> service.booksByAuthorName(null));
        assertThrows(ServiceException.class, () -> service.booksByAuthorName(" "));

        when(repository.findAuthorsByName("tom")).thenReturn(List.of(AuthorTest.AUTHOR));
        when(repository.findBooksByAuthorId(AuthorTest.AUTHOR.id())).thenReturn(List.of());
        final List<Book> books = service.booksByAuthorName("tom");

        verify(repository, times(1)).findAuthorsByName("tom");
        verify(repository, times(1)).findBooksByAuthorId(AuthorTest.AUTHOR.id());
        assertEquals(0, books.size());
    }
}