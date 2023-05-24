package com.putoet.mybooks.books.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.adapter.in.web.BookRequestAuthor;
import com.putoet.mybooks.books.adapter.in.web.BookResponse;
import com.putoet.mybooks.books.adapter.in.web.NewBookRequest;
import com.putoet.mybooks.books.domain.MimeTypes;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookFeatureStepdefs extends MyBooksE2EBase {
    private static final String TEMP_BOOKS = "tempBooks";

    public BookFeatureStepdefs(RestTemplate sslRestTemplate, ObjectMapper mapper) {
        super(sslRestTemplate, mapper);
    }

    @When("send a get request for books")
    public void sendAGetRequestForBooks() {
        executeGet("/books", null);
    }

    @And("response contains details on more than {int} book")
    public void responseContainsDetailsOnMoreThanBook(int minNumberOfBooksReturned) throws JsonProcessingException {
        final List<BookResponse> books = mapper.readValue(context.response().getBody(), new TypeReference<>() {
        });
        assertTrue(books.size() > minNumberOfBooksReturned);
    }

    @Given("a temp book with title {string} was created")
    public void aTempBookWasCreated(String title) throws JsonProcessingException {
        final BookRequestAuthor bookRequestAuthor = new BookRequestAuthor(null, "Author, " + title, Map.of());
        final NewBookRequest newBookRequest = new NewBookRequest("UUID",
                UUID.randomUUID().toString(),
                title,
                Set.of(bookRequestAuthor),
                Set.of("keyword"),
                Set.of(MimeTypes.EPUB.toString()));

        executePost("/book", mapper.writeValueAsString(newBookRequest), true);

        final BookResponse book = mapper.readValue(context.response().getBody(), BookResponse.class);
        if (context.get(TEMP_BOOKS, Set.class) == null)
            context.set(TEMP_BOOKS, new HashSet<BookResponse>());
        ((Set<BookResponse>) context.get(TEMP_BOOKS, Set.class)).add(book);
    }

    @And("books contain book with title {string}")
    public void booksContainBookWithTitle(String title) throws JsonProcessingException {
        final List<BookResponse> books = mapper.readValue(context.response().getBody(), new TypeReference<>() {});
        assertTrue(books.stream().anyMatch(book -> title.equals(book.title())));
    }

    @When("send a get request for books from author with name {string}")
    public void sendAGetRequestForBooksFromAuthorWithName(String name) {
        name = translateParameter(name);
        executeGet("/books/author/" + name, null);
    }

    @When("send a get request for books with title {string}")
    public void sendAGetRequestForBooksWithTitle(String title) {
        title = translateParameter(title);
        executeGet("/books/" + title, null);
    }

    @When("send a get request for temp book with id")
    public void sendAGetRequestForTempBookWithId() throws JsonProcessingException {
        final BookResponse book = mapper.readValue(context.response().getBody(), BookResponse.class);
        final String schema = book.schema();
        final String id = book.id();
        executeGet("/book/" + schema + "/" + id, null);
    }

    @When("send a get request for book with schema {word} and id {word}")
    public void sendAGetRequestForBookWithId(String schema, String id) {
        executeGet("/book/" + schema + "/" + id, null);
    }

    @And("book has title {string}")
    public void bookHasTitle(String title) throws JsonProcessingException {
        final BookResponse book = mapper.readValue(context.response().getBody(), BookResponse.class);
        assertEquals(title, book.title());
    }
}
