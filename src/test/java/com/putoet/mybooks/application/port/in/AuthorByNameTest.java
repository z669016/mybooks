package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.application.AuthorService;
import com.putoet.mybooks.application.port.out.AuthorRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthorByNameTest {
    private final Author author = AuthorTest.author;
    private final String name = author.name();

    private AuthorRepository authorRepository;
    private AuthorByName authorByName;

    @BeforeEach
    void setup() {
        authorRepository = mock(AuthorRepository.class);
        authorByName = new AuthorService(authorRepository);
    }

    @Test
    void authorByNameFound() {
        given(authorRepository.findAuthorByName(name)).willReturn(List.of(author));

        final var found = authorByName.authorByName(name);
        assertEquals(1, found.size());
        assertEquals(author, found.get(0));
    }

    @Test
    void authorByNameNotFound() {
        given(authorRepository.findAuthorByName(any())).willReturn(List.of());

        final var found = authorByName.authorByName(name);
        assertTrue(found.isEmpty());
    }

    @Test
    void error() {
        assertThrows(NullPointerException.class, () -> authorByName.authorByName(null));
        assertThrows(IllegalArgumentException.class, () -> authorByName.authorByName(""));
    }
}