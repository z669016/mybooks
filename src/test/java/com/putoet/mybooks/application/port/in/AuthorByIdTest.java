package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.application.BookInquiryService;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import com.putoet.mybooks.domain.AuthorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthorByIdTest {
    private final Author author = AuthorTest.author;
    private final AuthorId id = author.id();

    private BookRepository authorRepository;
    private AuthorById authorById;

    @BeforeEach
    void setup() {
        authorRepository = mock(BookRepository.class);
        authorById = new BookInquiryService(authorRepository);
    }

    @Test
    void authorByIdFound() {
        given(authorRepository.findAuthorById(id)).willReturn(author);

        final var found = authorById.authorById(id);
        assertTrue(found.isPresent());
        assertEquals(author, found.get());
    }

    @Test
    void authorByIdNotFound() {
        given(authorRepository.findAuthorById(any())).willReturn(null);

        final var found = authorById.authorById(id);
        assertTrue(found.isEmpty());
    }

    @Test
    void error() {
        assertThrows(NullPointerException.class, () -> authorById.authorById(null));
    }
}