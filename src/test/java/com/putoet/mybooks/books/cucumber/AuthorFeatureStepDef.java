package com.putoet.mybooks.books.cucumber;

import com.putoet.mybooks.books.adapter.in.web.AuthorResponse;
import com.putoet.mybooks.books.adapter.in.web.NewAuthorRequest;
import com.putoet.mybooks.books.adapter.in.web.UpdateAuthorRequest;
import com.putoet.mybooks.books.adapter.in.web.security.JwtRequestFilter;
import com.putoet.mybooks.books.domain.SiteType;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthorFeatureStepDef extends MyBooksE2EBase {
    private static final String TEMP_AUTHOR = "temp_author";

    @Before
    public void setup() {
        context.clear();
    }

    @When("send a get request for authors")
    public void sendAGetRequestForAuthors() {
        executeGet("/authors");
    }

    @And("response contains details on more than {int} authors")
    public void responseContainsDetailsOnMoreThanAuthors(int minNumberOfAuthorsReturned) {
        final var authors = context.response()
                .body()
                .jsonPath()
                .getList(".", AuthorResponse.class);
        assertTrue(authors.size() > minNumberOfAuthorsReturned);
    }

    @When("send a get request for author with id {word}")
    public void sendAGetRequestForAuthorWithId(String uuid) {
        executeGet("/author/" + uuid);
    }

    @And("author has id {word}")
    public void authorHasId(String uuid) {
        context.response()
                .then()
                .assertThat()
                .body("id", equalTo(uuid));
    }

    @And("author has name {string}")
    public void authorHasName(String name) {
        context.response()
                .then()
                .assertThat()
                .body("name", equalTo(name));
    }

    @When("sent a post request for a new author with name {string} and sites")
    public void sentAPostRequestForANewAuthorWithNameAndSites(String name, DataTable table) {
        final var sites = table.asMap();
        final var request = new NewAuthorRequest(name, sites);
        executePost("/author", request);
    }

    @And("author has sites")
    public void authorHasSites(DataTable table) {
        final var sites = table.asMap();
        final var author = context.response().body().as(AuthorResponse.class);
        assertEquals(sites, author.sites());
    }

    @Given("a created temp author")
    public void aCreatedTempAuthor() {
        final var newAuthorRequest = new NewAuthorRequest("name", Map.of(SiteType.HOMEPAGE_NAME, "https://www.google.com"));
        final var response = given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + context.token())
                .body(newAuthorRequest)
                .when()
                .post("https://localhost:443/author");
        context.set(TEMP_AUTHOR, response.body().as(AuthorResponse.class));
    }

    @When("sent a put request for temp author with new name {string}")
    public void sentAPutRequestForTempAuthorWithNewName(String name) {
        final var tempAuthor = context.get(TEMP_AUTHOR, AuthorResponse.class);
        final var update = new UpdateAuthorRequest(tempAuthor.version(), name);
        executePut("/author/" + tempAuthor.id(), update);
    }
}
