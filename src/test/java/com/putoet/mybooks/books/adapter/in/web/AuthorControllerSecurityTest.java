package com.putoet.mybooks.books.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.SiteType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorControllerSecurityTest {

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

    @WithMockUser(username = "user", roles = "USER")
    @Test
    void getAuthorsAuthenticated() throws Exception {
        mvc.perform(get("/authors").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAuthorsUnAuthenticated() throws Exception {
        mvc.perform(get("/authors").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "user", roles = "USER")
    @Test
    void getAuthorByIdAuthenticated() throws Exception {
        mvc.perform(get("/author/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAuthorByIdUnAuthenticated() throws Exception {
        mvc.perform(get("/author/123456").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "user", roles = "USER")
    @Test
    void getAuthorsByNameAuthenticated() throws Exception {
        mvc.perform(get("/authors/XYZ").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAuthorsByNameUnAuthenticated() throws Exception {
        mvc.perform(get("/authors/XYZ").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "user", roles = "USER")
    @Test
    void deleteAuthorByIdAuthenticated() throws Exception {
        mvc.perform(delete("/author/" + UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAuthorByIdUnAuthenticated() throws Exception {
        mvc.perform(delete("/author/123456").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "user", roles = "USER")
    @Test
    void createAuthorAuthenticated() throws Exception {
        final var author = new NewAuthorRequest("Schrijver, Jaap", Map.of(SiteType.HOMEPAGE_NAME, "https://goole.com"));
        when(bookManagementUpdatePort.registerAuthor(eq(author.name()), any())).thenReturn(new Author(AuthorId.withoutId(), author.name()));

        mvc.perform(post("/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isCreated());
    }

    @Test
    void createAuthorUnAuthenticated() throws Exception {
        mvc.perform(post("/author").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "user", roles = "USER")
    @Test
    void updateAuthorAuthenticated() throws Exception {
        final var id = UUID.randomUUID().toString();
        final var version = Instant.now();
        final var author = new UpdateAuthorRequest(version.toString(), "Schrijver, Jaap");
        when(bookManagementUpdatePort.updateAuthor(eq(AuthorId.withId(id)), eq(version), eq(author.name()))).thenReturn(new Author(AuthorId.withId(id), author.name()));

        mvc.perform(put("/author/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isOk());
    }

    @Test
    void updateAuthorUnAuthenticated() throws Exception {
        final var author = new UpdateAuthorRequest(Instant.now().toString(), "Schrijver, Jaap");

        mvc.perform(put("/author/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isForbidden());
    }
}