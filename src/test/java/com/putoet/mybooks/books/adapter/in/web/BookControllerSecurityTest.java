package com.putoet.mybooks.books.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerSecurityTest {

    // Mocking the ports is necessary to prevent the Spring context from loading the actual beans.
    @MockBean
    private BookManagementInquiryPort bookManagementInquiryPort;

    @MockBean
    private BookManagementUpdatePort bookManagementUpdatePort;

    // Mocking the DataSourceScriptDatabaseInitializer is required to prevent the database gets recreated and
    // the data gets reloaded. This is probably a work-around and I'm probably doing something wrong elsewhere
    @MockBean
    private DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getBooksAuthenticated() throws Exception {
        mvc.perform(get("/books").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBooksUnAuthenticated() throws Exception {
        mvc.perform(get("/books").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getBooksByAuthorNameAuthenticated() throws Exception {
        mvc.perform(get("/books/author/name").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBooksByAuthorNameUnAuthenticated() throws Exception {
        mvc.perform(get("/books/author/name").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getBooksByTitleAuthenticated() throws Exception {
        mvc.perform(get("/books/title").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBooksByTitleUnAuthenticated() throws Exception {
        mvc.perform(get("/books/title").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getBooksByIdAuthenticated() throws Exception {
        final var schema = BookId.BookIdSchema.UUID.toString();
        final var id = UUID.randomUUID().toString();

        mvc.perform(get("/book/" + schema + "/" + id).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBooksByIdUnAuthenticated() throws Exception {
        final var schema = BookId.BookIdSchema.UUID.toString();
        final var id = UUID.randomUUID().toString();

        mvc.perform(get("/book/" + schema + "/" + id).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void createBookAuthenticated() throws Exception {
        final var bookRequestAuthor = getBookRequestAuthor();
        final var newBookRequest = getNewBookRequest(bookRequestAuthor);

        when(bookManagementInquiryPort.authorById(eq(AuthorId.withId(bookRequestAuthor.id()))))
                .thenReturn(Optional.of(new Author(AuthorId.withId(bookRequestAuthor.id()), bookRequestAuthor.name())));
        when(bookManagementUpdatePort.registerBook(eq(new BookId(newBookRequest.schema(), newBookRequest.id())), any(), any(), any(), any()))
                .thenReturn(new Book(new BookId(newBookRequest.schema(), newBookRequest.id()), newBookRequest.title(), Set.of(), Set.of(), Set.of()));
        mvc.perform(post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createBooksUnAuthenticated() throws Exception {
        final var bookRequestAuthor = getBookRequestAuthor();
        final var newBookRequest = getNewBookRequest(bookRequestAuthor);

        mvc.perform(post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookRequest)))
                .andExpect(status().isForbidden());
    }

    private static NewBookRequest getNewBookRequest(BookRequestAuthor bookRequestAuthor) {
        return new NewBookRequest(BookId.BookIdSchema.UUID.name(),
                UUID.randomUUID().toString(),
                "Title",
                Set.of(bookRequestAuthor),
                Set.of("word1", "word2"),
                Set.of(MimeTypes.EPUB.toString())
        );
    }

    private static BookRequestAuthor getBookRequestAuthor() {
        return new BookRequestAuthor(UUID.randomUUID().toString(), "Schrijver, Jaap de", Map.of());
    }
}