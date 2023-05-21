package com.putoet.mybooks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.adapter.in.web.AuthorResponse;
import com.putoet.mybooks.books.adapter.in.web.NewAuthorRequest;
import com.putoet.mybooks.books.adapter.in.web.UpdateAuthorRequest;
import com.putoet.mybooks.books.domain.SiteType;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthorFeatureStepDef extends MyBooksE2EBase {
    private static final String TEMP_AUTHOR = "temp_author";

    public AuthorFeatureStepDef(RestTemplate sslRestTemplate, ObjectMapper mapper) {
        super(sslRestTemplate, mapper);
    }

    @Before
    public void setup() {
        context.clear();
    }

    @When("send a get request for authors")
    public void sendAGetRequestForAuthors() {
        executeGet("/authors", null);
    }

    @And("response contains details on more than {int} authors")
    public void responseContainsDetailsOnMoreThanAuthors(int minNumberOfAuthorsReturned) throws JsonProcessingException {
        final List<AuthorResponse> authors = mapper.readValue(context.response().getBody(), new TypeReference<>() {});
        assertTrue(authors.size() >= minNumberOfAuthorsReturned);
    }

    @When("send a get request for author with id {word}")
    public void sendAGetRequestForAuthorWithId(String uuid) {
        executeGet("/author/" + uuid, null);
    }

    @And("author has id {word}")
    public void authorHasId(String uuid) throws JsonProcessingException {
        final AuthorResponse author = mapper.readValue(context.response().getBody(), AuthorResponse.class);
        assertEquals(uuid, author.id());
    }

    @And("author has name {string}")
    public void authorHasName(String name) throws JsonProcessingException {
        final AuthorResponse author = mapper.readValue(context.response().getBody(), AuthorResponse.class);
        assertEquals(name, author.name());
    }

    @When("sent a post request for a new author with name {string} and sites")
    public void sentAPostRequestForANewAuthorWithNameAndSites(String name, DataTable table) throws JsonProcessingException {
        final Map<String,String> sites = table.asMap();
        final NewAuthorRequest request = new NewAuthorRequest(name, sites);
        executePost("/author", mapper.writeValueAsString(request), true);
    }

    @And("author has sites")
    public void authorHasSites(DataTable table) throws JsonProcessingException {
        final Map<String,String> sites = table.asMap();
        final AuthorResponse author = mapper.readValue(context.response().getBody(), AuthorResponse.class);
        assertEquals(sites, author.sites());
    }

    @Given("a created temp author")
    public void aCreatedTempAuthor() throws JsonProcessingException {
        final NewAuthorRequest newAuthorRequest = new NewAuthorRequest("name", Map.of(SiteType.HOMEPAGE_NAME, "https://www.google.com"));
        executePost("/author", mapper.writeValueAsString(newAuthorRequest), true);
        context.set(TEMP_AUTHOR, mapper.readValue(context.response().getBody(), AuthorResponse.class));
    }

    @When("sent a put request for temp author with new name {string}")
    public void sentAPutRequestForTempAuthorWithNewName(String name) throws JsonProcessingException {
        final AuthorResponse tempAuthor = context.get(TEMP_AUTHOR, AuthorResponse.class);
        final UpdateAuthorRequest update = new UpdateAuthorRequest(tempAuthor.version(), name);
        executePut("/author/" + tempAuthor.id(), mapper.writeValueAsString(update), true);
    }
}
