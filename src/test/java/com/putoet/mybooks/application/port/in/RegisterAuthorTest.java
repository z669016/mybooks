package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.application.BookService;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterAuthorTest {
    private final Author author = AuthorTest.author;
    private final RegisterAuthorCommand command = new RegisterAuthorCommand(author.name(), author.sites());

    private BookRepository authorRepository;
    private RegisterAuthor registerAuthor;

    @BeforeEach
    void setup() {
        authorRepository = mock(BookRepository.class);
        registerAuthor = new BookService(authorRepository);
    }

    @Test
    void registerAuthor() {
        when(authorRepository.persist(any())).thenAnswer((Answer<Author>) invocation -> (Author) invocation.getArguments()[0]);

        final var registered = registerAuthor.registerAuthor(command);
        assertNotNull(registered.id());
        assertEquals(author.name(), registered.name());
        assertEquals(author.sites(), registered.sites());
    }

    @Test
    void registerAuthorFailed() {
        given(authorRepository.findAuthorsByName(any())).willReturn(null);

        assertThrows(IllegalStateException.class, () -> registerAuthor.registerAuthor(command));
    }

    @Test
    void error() {
        assertThrows(NullPointerException.class, () -> registerAuthor.registerAuthor(null));
    }
}