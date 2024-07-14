package com.putoet.mybooks.books.cucumber;

import com.putoet.mybooks.books.adapter.in.web.BookRequestAuthor;
import com.putoet.mybooks.books.adapter.in.web.BookResponse;
import com.putoet.mybooks.books.adapter.in.web.NewBookRequest;
import com.putoet.mybooks.books.domain.MimeTypes;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookFeatureStepDef extends MyBooksE2EBase {
    private static final String TEMP_BOOKS = "tempBooks";

    @Before
    public void setup() {
        context.clear();
    }

    @When("send a get request for books")
    public void sendAGetRequestForBooks() {
        executeGet("/books");
    }

    @And("response contains details on more than {int} book")
    public void responseContainsDetailsOnMoreThanBook(int minNumberOfBooksReturned) {
        final var books = context.response()
                .body()
                .jsonPath()
                .getList(".", BookResponse.class);
        assertTrue(books.size() > minNumberOfBooksReturned);
    }

    @Given("a temp book with title {string} was created")
    public void aTempBookWasCreated(String title) {
        final var bookRequestAuthor = new BookRequestAuthor(null, "Author, " + title, Map.of());
        final var newBookRequest = new NewBookRequest("UUID",
                UUID.randomUUID().toString(),
                title,
                Set.of(bookRequestAuthor),
                Set.of("keyword"),
                Set.of(MimeTypes.EPUB.toString()));

        executePost("/book", newBookRequest, true);

        final var book = context.response().body().as(BookResponse.class);
        if (context.get(TEMP_BOOKS, Set.class) == null)
            context.set(TEMP_BOOKS, new HashSet<BookResponse>());
        //noinspection unchecked
        ((Set<BookResponse>) context.get(TEMP_BOOKS, Set.class)).add(book);
    }


    @And("books contain book with title {string}")
    public void booksContainBookWithTitle(String title) {
        final var books = context.response().body().jsonPath().getList(".", BookResponse.class);
        assertTrue(books.stream().anyMatch(book -> title.equals(book.title())));
    }

    @When("send a get request for books from author with name {string}")
    public void sendAGetRequestForBooksFromAuthorWithName(String name) {
        name = translateParameter(name);
        executeGet("/books/author/" + name);
    }

    @When("send a get request for books with title {string}")
    public void sendAGetRequestForBooksWithTitle(String title) {
        title = translateParameter(title);
        executeGet("/books/" + title);
    }

    @When("send a get request for temp book with id")
    public void sendAGetRequestForTempBookWithId() {
        final var book = context.response().body().as(BookResponse.class);
        final var schema = book.schema();
        final var id = book.id();
        executeGet("/book/" + schema + "/" + id);
    }

    @When("send a get request for book with schema {word} and id {word}")
    public void sendAGetRequestForBookWithId(String schema, String id) {
        executeGet("/book/" + schema + "/" + id);
    }

    @And("book has title {string}")
    public void bookHasTitle(String title) {
        context.response()
                .then()
                .assertThat()
                .body("title", equalTo(title));
    }
}
