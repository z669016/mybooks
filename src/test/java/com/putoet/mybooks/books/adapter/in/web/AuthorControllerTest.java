package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.BookInquiryService;
import com.putoet.mybooks.books.application.BookUpdateService;
import com.putoet.mybooks.books.application.port.in.ServiceError;
import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorControllerTest {
    private BookInquiryService bookInquiryService;
    private BookUpdateService bookUpdateService;
    private AuthorController authorController;

    private final Author author = new Author(AuthorId.withoutId(), "Schrijver, Jaap de");

    @BeforeEach
    void setup() {
        bookInquiryService = mock(BookInquiryService.class);
        bookUpdateService = mock(BookUpdateService.class);
        authorController = new AuthorController(bookInquiryService, bookUpdateService);
    }

    @Test
    void getAuthors() {
        final List<AuthorResponse> authors = authorController.getAuthors();
        assertAll(
                () -> assertEquals(0, authors.size()),
                () -> verify(bookInquiryService, times(1)).authors()
        );
    }

    @Test
    void getAuthorsFailed() {
        when(bookInquiryService.authors()).thenThrow(new RuntimeException("FAIL"));
        try {
            authorController.getAuthors();
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertAll(
                    () -> verify(bookInquiryService, times(1)).authors(),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode())
            );
        }
    }

    @Test
    void getAuthorById() {
        when(bookInquiryService.authorById(author.id())).thenReturn(Optional.of(author));
        authorController.getAuthorById(author.id().uuid().toString());
        verify(bookInquiryService, times(1)).authorById(author.id());
    }

    @Test
    void getAuthorByIdFailed() {
        when(bookInquiryService.authorById(author.id())).thenThrow(new RuntimeException("FAIL"));
        try {
            authorController.getAuthorById(author.id().uuid().toString());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void getAuthorByIdNotFound() {
        when(bookInquiryService.authorById(author.id())).thenReturn(Optional.empty());
        try {
            authorController.getAuthorById(author.id().uuid().toString());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.NOT_FOUND, exc.getStatusCode());
        }
    }

    @Test
    void getAuthorsByName() {
        when(bookInquiryService.authorsByName(author.name())).thenReturn(List.of(author));
        authorController.getAuthorsByName(author.name());
        verify(bookInquiryService, times(1)).authorsByName(author.name());
    }

    @Test
    void getAuthorByNameFailed() {
        when(bookInquiryService.authorsByName(author.name())).thenThrow(new RuntimeException("FAIL"));
        try {
            authorController.getAuthorsByName(author.name());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void deleteAuthorById() {
        authorController.deleteAuthorById(author.id().uuid().toString());
        verify(bookUpdateService, times(1)).forgetAuthor(author.id());
    }

    @Test
    void deleteAuthorByIdFailed() {
        doThrow(new ServiceException(ServiceError.AUTHOR_NOT_UPDATED)).when(bookUpdateService).forgetAuthor(author.id());
        try {
            authorController.deleteAuthorById(null);
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }

        try {
            authorController.deleteAuthorById(author.id().uuid().toString());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void postAuthor() {
        when(bookUpdateService.registerAuthor(author.name(), Map.of())).thenReturn(author);
        authorController.postAuthor(new NewAuthorRequest(author.name(), Map.of()));
        verify(bookUpdateService, times(1)).registerAuthor(author.name(), Map.of());
    }

    @Test
    void postAuthorFailed() {
        when(bookUpdateService.registerAuthor(author.name(), Map.of())).thenThrow(new ServiceException(ServiceError.AUTHOR_NOT_UPDATED));

        try {
            authorController.postAuthor(new NewAuthorRequest(null, null));
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }

        try {
            authorController.postAuthor(new NewAuthorRequest("  ", null));
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }

        try {
            authorController.postAuthor(new NewAuthorRequest("name", null));
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void putAuthor() {
        when(bookUpdateService.updateAuthor(author.id(), author.version(),author.name())).thenReturn(author);
        authorController.putAuthor(author.id().uuid().toString(), new UpdateAuthorRequest(author.version().toString(), author.name()));
        verify(bookUpdateService, times(1)).updateAuthor(author.id(), author.version(), author.name());
    }

    @Test
    void putAuthorFailed() {
        when(bookUpdateService.updateAuthor(author.id(), author.version(),author.name())).thenThrow(new ServiceException(ServiceError.AUTHOR_NOT_UPDATED));

        try {
            authorController.putAuthor(null, new UpdateAuthorRequest(null, null));
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }

        try {
            authorController.putAuthor(null, new UpdateAuthorRequest(author.version().toString(), null));
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }

        try {
            authorController.putAuthor(null, new UpdateAuthorRequest(author.version().toString(), "  "));
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }
}