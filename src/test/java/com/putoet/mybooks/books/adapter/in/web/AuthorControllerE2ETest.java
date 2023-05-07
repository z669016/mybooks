package com.putoet.mybooks.books.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.MybooksApplication;
import com.putoet.mybooks.books.domain.SiteType;
import com.putoet.mybooks.books.domain.validation.ObjectIDConstraint;
import com.putoet.mybooks.books.domain.validation.SiteMapConstraint;
import com.putoet.mybooks.books.domain.validation.StandardValidations;
import com.putoet.mybooks.books.domain.validation.VersionConstraint;
import jakarta.validation.constraints.NotBlank;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MybooksApplication.class)
@AutoConfigureMockMvc
class AuthorControllerE2ETest {

    @Autowired
    private MockMvc mvc;

    private MockRequest mockRequest;
    private String userToken;
    private ObjectMapper mapper;

    @BeforeEach
     void init() throws Exception {
        mockRequest = new MockRequest(mvc);
        mapper = new ObjectMapper();
        userToken = mockRequest.userToken();
    }

    @Test
    void getAuthorWithBlankIdFails() throws Exception {
        mvc.perform(mockRequest.jwtGetRequestWithToken("/author/  ", userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.startsWith("getAuthorById.id: " + ObjectIDConstraint.ID_ERROR)));
    }

    @Test
    void postAuthor() throws Exception {
        final NewAuthorRequest newAuthorRequest = new NewAuthorRequest("name", Map.of(SiteType.HOMEPAGE_NAME, "https://www.google.com"));
        final String json = mapper.writeValueAsString(newAuthorRequest);
        mvc.perform(mockRequest.jwtPostRequestWithToken("/author", userToken, json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newAuthorRequest.name()));
    }

    @Test
    void postAuthorWithInvalidNameFails() throws Exception {
        NewAuthorRequest newAuthorRequest = new NewAuthorRequest(null, null);
        String json = mapper.writeValueAsString(newAuthorRequest);
        mvc.perform(mockRequest.jwtPostRequestWithToken("/author", userToken, json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.name").value(StandardValidations.message(NotBlank.class)))
                .andExpect(jsonPath("$.error.sites").value(SiteMapConstraint.SITEMAP_ERROR));

        newAuthorRequest = new NewAuthorRequest("name", Map.of("  ", "https://www.google.com"));
        json = mapper.writeValueAsString(newAuthorRequest);
        mvc.perform(mockRequest.jwtPostRequestWithToken("/author", userToken, json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.sites").value(SiteMapConstraint.SITEMAP_ERROR));

        newAuthorRequest = new NewAuthorRequest("name", Map.of("google", "bla"));
        json = mapper.writeValueAsString(newAuthorRequest);
        mvc.perform(mockRequest.jwtPostRequestWithToken("/author", userToken, json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.sites").value(SiteMapConstraint.SITEMAP_ERROR));
    }

    @Test
    void putAuthor() throws Exception {
        final AuthorResponse author = newTempAuthor();

        final UpdateAuthorRequest updateAuthorRequest = new UpdateAuthorRequest(author.version(), "new name");
        final String json = mapper.writeValueAsString(updateAuthorRequest);
        mvc.perform(mockRequest.jwtPutRequestWithToken("/author/" + author.id(), userToken, json))
                .andExpect(status().isOk())
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.name").value("new name"));
    }

    @Test
    void putAuthorFails() throws Exception {
        final AuthorResponse author = newTempAuthor();

        UpdateAuthorRequest updateAuthorRequest = new UpdateAuthorRequest(null, null);
        String json = mapper.writeValueAsString(updateAuthorRequest);
        mvc.perform(mockRequest.jwtPutRequestWithToken("/author/" + author.id(), userToken, json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.name").value(StandardValidations.message(NotBlank.class)))
                .andExpect(jsonPath("$.error.version").value(VersionConstraint.VERSION_ERROR));

        updateAuthorRequest = new UpdateAuthorRequest("bla", "  ");
        json = mapper.writeValueAsString(updateAuthorRequest);
        mvc.perform(mockRequest.jwtPutRequestWithToken("/author/" + author.id(), userToken, json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.name").value(StandardValidations.message(NotBlank.class)))
                .andExpect(jsonPath("$.error.version").value(VersionConstraint.VERSION_ERROR));
    }

    private AuthorResponse newTempAuthor() throws Exception {
        final NewAuthorRequest newAuthorRequest = new NewAuthorRequest("name", Map.of(SiteType.HOMEPAGE_NAME, "https://www.google.com"));
        final String body = mvc.perform(mockRequest.jwtPostRequestWithToken("/author", userToken, mapper.writeValueAsString(newAuthorRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readValue(body, AuthorResponse.class);
    }
}
