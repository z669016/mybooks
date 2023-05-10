package com.putoet.mybooks.books.adapter.in.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.MybooksApplication;
import com.putoet.mybooks.books.adapter.in.web.validation.ExistingBookRequestConstraint;
import com.putoet.mybooks.books.domain.MimeTypes;
import com.putoet.mybooks.books.domain.SiteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MybooksApplication.class)
@AutoConfigureMockMvc
class BookControllerE2ETest {

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
    void getBooks() throws Exception {
        final BookResponse book1 = createTempBook("getBooks-1");
        final BookResponse book2 = createTempBook("getBooks-2");

        final String body = mvc.perform(mockRequest.jwtGetRequestWithToken("/books", userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final List<BookResponse> books = mapper.readValue(body, new TypeReference<>() {});
        assertAll(
                () -> assertTrue(books.size() >= 2),
                () -> assertTrue(books.contains(book1)),
                () -> assertTrue(books.contains(book2))
        );
    }

    @Test
    void getBookByAuthorName() throws Exception {
        final BookResponse book1 = createTempBook("getBookByAuthor-1");
        final BookResponse book2 = createTempBook("getBookByAuthor-2");
        final BookResponse book3 = createTempBook("getBookByAutho-3 should not be found");

        final String body = mvc.perform(mockRequest.jwtGetRequestWithToken("/books/author/" + "getBookByAuthor", userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final List<BookResponse> books = mapper.readValue(body, new TypeReference<>() {});
        assertAll(
                () -> assertEquals(2, books.size()),
                () -> assertTrue(books.contains(book1)),
                () -> assertTrue(books.contains(book2)),
                () -> assertFalse(books.contains(book3))
        );
    }

    @Test
    void getBookByAuthorNameFails() throws Exception {
        mvc.perform(mockRequest.jwtGetRequestWithToken("/books/author/" + "   ", userToken))
                .andExpect(status().isBadRequest())
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.error").value("getBooksByAuthorName.name: must not be blank"));
    }

    @Test
    void getBookByTitle() throws Exception {
        final BookResponse book1 = createTempBook("getBookByTitle-1");
        final BookResponse book2 = createTempBook("getBookByTitle-2");
        final BookResponse book3 = createTempBook("getBookByTitl-3 should not be found");

        final String body = mvc.perform(mockRequest.jwtGetRequestWithToken("/books/" + "getBookByTitle", userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final List<BookResponse> books = mapper.readValue(body, new TypeReference<>() {});
        assertAll(
                () -> assertEquals(2, books.size()),
                () -> assertTrue(books.contains(book1)),
                () -> assertTrue(books.contains(book2)),
                () -> assertFalse(books.contains(book3))
        );
    }

    @Test
    void getBookByTitleFails() throws Exception {
        mvc.perform(mockRequest.jwtGetRequestWithToken("/books/" + "   ", userToken))
                .andExpect(status().isBadRequest())
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.error").value("getBooksByTitle.title: must not be blank"));
    }

    @Test
    void getBookById() throws Exception {
        final BookResponse book = createTempBook("getBookById");

        final String body = mvc.perform(mockRequest.jwtGetRequestWithToken("/book/" + book.schema() + "/" + book.id(), userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final BookResponse found = mapper.readValue(body, BookResponse.class);
        assertEquals(book, found);
    }

    @Test
    void getBookByIdFails() throws Exception {
        final BookResponse book = createTempBook("getBookByIdFails");
        mvc.perform(mockRequest.jwtGetRequestWithToken("/book/" + book.schema() + "/" + "bla", userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.parameters").value(ExistingBookRequestConstraint.BOOK_REQUEST_ERROR));
    }

    @Test
    public void postBook() throws Exception {
        final BookRequestAuthor bookRequestAuthor = new BookRequestAuthor(null,"name", Map.of(SiteType.HOMEPAGE_NAME, "https://www.google.com"));
        final NewBookRequest newBookRequest = new NewBookRequest("UUID",
                UUID.randomUUID().toString(),
                "New book",
                List.of(bookRequestAuthor),
                Set.of("keyword"),
                List.of(MimeTypes.EPUB.toString()));

        final String body = mvc.perform(mockRequest.jwtPostRequestWithToken("/book", userToken, mapper.writeValueAsString(newBookRequest)))
                .andExpect(status().isOk())
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mapper.readValue(body, BookResponse.class);
    }

    private BookResponse createTempBook(String title) throws Exception {
        final BookRequestAuthor bookRequestAuthor = new BookRequestAuthor(null, "Author, " + title, Map.of());
        final NewBookRequest newBookRequest = new NewBookRequest("UUID",
                UUID.randomUUID().toString(),
                title,
                List.of(bookRequestAuthor),
                Set.of("keyword"),
                List.of(MimeTypes.EPUB.toString()));

        final String result = mvc.perform(mockRequest.jwtPostRequestWithToken("/book", userToken, mapper.writeValueAsString(newBookRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return mapper.readValue(result, BookResponse.class);
    }
}
