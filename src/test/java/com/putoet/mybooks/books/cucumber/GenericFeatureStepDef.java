package com.putoet.mybooks.books.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.adapter.in.web.ApiError;
import com.putoet.mybooks.books.adapter.in.web.security.JwtResponse;
import com.putoet.mybooks.books.adapter.in.web.security.UserLoginRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals(statusCodeValue, context.response().getStatusCode().value());
    }

    @And("errors contains {word}")
    public void errorsContainsName(String field) throws JsonProcessingException {
        final ApiError apiError = mapper.readValue(context.response().getBody(), ApiError.class);
        assertTrue(apiError.errors().containsKey(field));
    }
}
