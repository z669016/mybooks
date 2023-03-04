package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.application.BookService;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterAuthorTest {
    private final Author author = AuthorTest.AUTHOR;
    private final RegisterAuthorCommand command = new RegisterAuthorCommand(author.name(), author.sites());

    private BookRepository bookRepository;
    private RegisterAuthor registerAuthor;

    @BeforeEach
    void setup() {
        bookRepository = mock(BookRepository.class);
        registerAuthor = new BookService(bookRepository);
    }

    @Test
    void registerAuthor() {
        when(bookRepository.createAuthor(author.name(),author.sites())).thenReturn(author);

        final var registered = registerAuthor.registerAuthor(command);
        assertNotNull(registered.id());
        assertEquals(author.name(), registered.name());
        assertEquals(author.sites(), registered.sites());
    }

    @Test
    void registerAuthorFailed() {
        given(bookRepository.findAuthorsByName(any())).willReturn(null);

        assertThrows(ServiceException.class, () -> registerAuthor.registerAuthor(command));
    }

    @Test
    void error() {
        assertThrows(ServiceException.class, () -> registerAuthor.registerAuthor(null));
    }
}