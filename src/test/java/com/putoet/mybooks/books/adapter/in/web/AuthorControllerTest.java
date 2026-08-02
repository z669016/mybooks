package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.application.port.in.ServiceError;
import com.putoet.mybooks.books.application.port.in.ServiceException;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
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
        final var exc = assertThrows(ResponseStatusException.class, () -> authorController.getAuthors());
        assertAll(
                () -> verify(bookManagementInquiryPort, times(1)).authors(),
                () -> assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode())
            );
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
        final var exc = assertThrows(ResponseStatusException.class, () -> authorController.getAuthorById(author.id().uuid().toString()));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
    }

    @Test
    void getAuthorByIdNotFound() {
        when(bookManagementInquiryPort.authorById(author.id())).thenReturn(Optional.empty());
        final var exc = assertThrows(ResponseStatusException.class, () -> authorController.getAuthorById(author.id().uuid().toString()));
        assertEquals(HttpStatus.NOT_FOUND, exc.getStatusCode());
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
        final var exc = assertThrows(ResponseStatusException.class, () -> authorController.getAuthorsByName(author.name()));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
    }

    @Test
    void deleteAuthorById() {
        authorController.deleteAuthorById(author.id().uuid().toString());
        verify(bookManagementUpdatePort, times(1)).forgetAuthor(author.id());
    }

    @Test
    void deleteAuthorByIdFailed() {
        doThrow(new ServiceException(ServiceError.AUTHOR_NOT_UPDATED)).when(bookManagementUpdatePort).forgetAuthor(author.id());
        var exc = assertThrows(ResponseStatusException.class, () -> authorController.deleteAuthorById(null));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());

        exc = assertThrows(ResponseStatusException.class, () -> authorController.deleteAuthorById(author.id().uuid().toString()));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
    }

    @Test
    void createAuthor() {
        when(bookManagementUpdatePort.registerAuthor(author.name(), Map.of())).thenReturn(author);
        authorController.createAuthor(new NewAuthorRequest(author.name(), Map.of()));
        verify(bookManagementUpdatePort, times(1)).registerAuthor(author.name(), Map.of());
    }

    @Test
    void createAuthorFailed() {
        var exc = assertThrows(ResponseStatusException.class, () -> authorController.createAuthor(new NewAuthorRequest(null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());

        exc = assertThrows(ResponseStatusException.class, () -> authorController.createAuthor(new NewAuthorRequest("  ", null)));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());

        exc = assertThrows(ResponseStatusException.class, () -> authorController.createAuthor(new NewAuthorRequest("name", null)));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
    }

    @Test
    void updateAuthor() {
        when(bookManagementUpdatePort.updateAuthor(author.id(), author.version(), author.name())).thenReturn(author);
        authorController.updateAuthor(author.id().uuid().toString(), new UpdateAuthorRequest(author.version().toString(), author.name()));
        verify(bookManagementUpdatePort, times(1)).updateAuthor(author.id(), author.version(), author.name());
    }

    @Test
    void updateAuthorFailed() {
        var exc = assertThrows(ResponseStatusException.class, () -> authorController.updateAuthor(null, new UpdateAuthorRequest(null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());

        exc = assertThrows(ResponseStatusException.class, () -> authorController.updateAuthor(null, new UpdateAuthorRequest(author.version().toString(), null)));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());

        exc = assertThrows(ResponseStatusException.class, () -> authorController.updateAuthor(null, new UpdateAuthorRequest(author.version().toString(), "  ")));
        assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
    }
}