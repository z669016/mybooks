package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.application.port.in.ServiceError;
import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    @Mock
    private BookManagementInquiryPort bookManagementInquiryPort;

    @Mock
    private BookManagementUpdatePort bookManagementUpdatePort;

    @InjectMocks
    private AuthorController authorController;

    private final Author author = new Author(AuthorId.withoutId(), "Schrijver, Jaap de");

    @Test
    void getAuthors() {
        final var authors = authorController.getAuthors();
        assertAll(
                () -> assertEquals(0, authors.size()),
                () -> verify(bookManagementInquiryPort, times(1)).authors()
        );
    }

    @Test
    void getAuthorsFailed() {
        when(bookManagementInquiryPort.authors()).thenThrow(new RuntimeException("FAIL"));
        try {
            authorController.getAuthors();
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertAll(
                    () -> verify(bookManagementInquiryPort, times(1)).authors(),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode())
            );
        }
    }

    @Test
    void getAuthorById() {
        when(bookManagementInquiryPort.authorById(author.id())).thenReturn(Optional.of(author));
        authorController.getAuthorById(author.id().uuid().toString());
        verify(bookManagementInquiryPort, times(1)).authorById(author.id());
    }

    @Test
    void getAuthorByIdFailed() {
        when(bookManagementInquiryPort.authorById(author.id())).thenThrow(new RuntimeException("FAIL"));
        try {
            authorController.getAuthorById(author.id().uuid().toString());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void getAuthorByIdNotFound() {
        when(bookManagementInquiryPort.authorById(author.id())).thenReturn(Optional.empty());
        try {
            authorController.getAuthorById(author.id().uuid().toString());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.NOT_FOUND, exc.getStatusCode());
        }
    }

    @Test
    void getAuthorsByName() {
        when(bookManagementInquiryPort.authorsByName(author.name())).thenReturn(Set.of(author));
        authorController.getAuthorsByName(author.name());
        verify(bookManagementInquiryPort, times(1)).authorsByName(author.name());
    }

    @Test
    void getAuthorByNameFailed() {
        when(bookManagementInquiryPort.authorsByName(author.name())).thenThrow(new RuntimeException("FAIL"));
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
        verify(bookManagementUpdatePort, times(1)).forgetAuthor(author.id());
    }

    @Test
    void deleteAuthorByIdFailed() {
        doThrow(new ServiceException(ServiceError.AUTHOR_NOT_UPDATED)).when(bookManagementUpdatePort).forgetAuthor(author.id());
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
        when(bookManagementUpdatePort.registerAuthor(author.name(), Map.of())).thenReturn(author);
        authorController.postAuthor(new NewAuthorRequest(author.name(), Map.of()));
        verify(bookManagementUpdatePort, times(1)).registerAuthor(author.name(), Map.of());
    }

    @Test
    void postAuthorFailed() {
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
        when(bookManagementUpdatePort.updateAuthor(author.id(), author.version(),author.name())).thenReturn(author);
        authorController.putAuthor(author.id().uuid().toString(), new UpdateAuthorRequest(author.version().toString(), author.name()));
        verify(bookManagementUpdatePort, times(1)).updateAuthor(author.id(), author.version(), author.name());
    }

    @Test
    void putAuthorFailed() {
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