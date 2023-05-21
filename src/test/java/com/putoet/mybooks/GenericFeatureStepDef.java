package com.putoet.mybooks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.adapter.in.web.ApiError;
import com.putoet.mybooks.books.adapter.in.web.security.JwtResponse;
import com.putoet.mybooks.books.adapter.in.web.security.UserLoginRequest;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class GenericFeatureStepDef extends MyBooksE2EBase {
    public GenericFeatureStepDef(RestTemplate sslRestTemplate, ObjectMapper mapper) {
        super(sslRestTemplate, mapper);
    }

    @Given("a successful user login")
    public void aSuccessfulUserLogin() throws IOException {
        userLogin();
    }

    @Given("a successful admin login")
    public void aSuccessfulAdminLogin() throws IOException {
        adminLogin();
    }

    @Then("the client receives status code of {int}")
    public void theClientReceivesStatusCodeOf(int statusCodeValue) {
        assertEquals(statusCodeValue, context.response().getStatusCode().value());
    }

    @And("errors contains {word}")
    public void errorsContainsName(String field) throws JsonProcessingException {
        final ApiError apiError = mapper.readValue(context.response().getBody(), ApiError.class);
        assertTrue(apiError.errors().containsKey(field));
    }

    public void userLogin() throws IOException {
        final UserLoginRequest login = new UserLoginRequest("putoet@outlook.com", "2password!");
        executePost("/login", mapper.writeValueAsString(login));
        assertEquals(context.response().getStatusCode(), HttpStatus.OK);

        final JwtResponse jwtResponse = mapper.readValue(context.response().getBody(), JwtResponse.class);
        assertNotNull(jwtResponse.access_token());
        context.token(jwtResponse.access_token());
    }

    public void adminLogin() throws IOException {
        final UserLoginRequest login = new UserLoginRequest("z669016@gmail.com", "1password!");
        executePost("/login", mapper.writeValueAsString(login));
        assertEquals(context.response().getStatusCode(), HttpStatus.OK);

        final JwtResponse jwtResponse = mapper.readValue(context.response().getBody(), JwtResponse.class);
        assertNotNull(jwtResponse.access_token());
        context.token(jwtResponse.access_token());
    }

}
