package com.putoet.mybooks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.adapter.in.web.ApiError;
import com.putoet.mybooks.books.adapter.in.web.AuthorResponse;
import com.putoet.mybooks.books.adapter.in.web.NewAuthorRequest;
import com.putoet.mybooks.books.adapter.in.web.UpdateAuthorRequest;
import com.putoet.mybooks.books.adapter.in.web.security.JwtResponse;
import com.putoet.mybooks.books.adapter.in.web.security.UserLoginRequest;
import com.putoet.mybooks.books.domain.SiteType;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserFeatureStepDef extends MyBooksE2EBase {
    public UserFeatureStepDef(RestTemplate sslRestTemplate, ObjectMapper mapper) {
        super(sslRestTemplate, mapper);
    }

    @Before
    public void setup() {
        context.clear();
    }

    @When("send a login request for user {string} with password {string}")
    public void sendALoginRequestForUserWithPassword(String id, String password) throws JsonProcessingException {
        final UserLoginRequest login = new UserLoginRequest(id, password);
        executePost("/login", mapper.writeValueAsString(login));
    }

    @And("response contains a token")
    public void responseContainsAToken() throws JsonProcessingException {
        final JwtResponse response = mapper.readValue(context.response().getBody(), JwtResponse.class);
    }

    @And("response authorization header contains bearer token")
    public void responseAuthorizationHeaderContainsBearerToken() {
    }

    @And("response cookie jwt is set with token")
    public void responseCookieJwtIsSetWithToken() {
    }
}
