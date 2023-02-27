package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.application.BookInquiryService;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthorsByNameTest {
    private final Author author = AuthorTest.author;
    private final String name = author.name();

    private BookRepository authorRepository;
    private AuthorsByName authorsByName;

    @BeforeEach
    void setup() {
        authorRepository = mock(BookRepository.class);
        authorsByName = new BookInquiryService(authorRepository);
    }

    @Test
    void authorByNameFound() {
        given(authorRepository.findAuthorsByName(name)).willReturn(List.of(author));

        final var found = authorsByName.authorsByName(name);
        assertEquals(1, found.size());
        assertEquals(author, found.get(0));
    }

    @Test
    void authorByNameNotFound() {
        given(authorRepository.findAuthorsByName(any())).willReturn(List.of());

        final var found = authorsByName.authorsByName(name);
        assertTrue(found.isEmpty());
    }

    @Test
    void error() {
        assertThrows(NullPointerException.class, () -> authorsByName.authorsByName(null));
        assertThrows(IllegalArgumentException.class, () -> authorsByName.authorsByName(""));
    }
}