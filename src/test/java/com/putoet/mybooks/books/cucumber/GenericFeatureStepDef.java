package com.putoet.mybooks.books.cucumber;

import com.putoet.mybooks.books.adapter.in.web.ApiError;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenericFeatureStepDef extends MyBooksE2EBase {

    @Given("a successful user login")
    public void aSuccessfulUserLogin() {
        userLogin();
    }

    @Given("a successful admin login")
    public void aSuccessfulAdminLogin() {
        adminLogin();
    }

    @Then("the client receives status code of {int}")
    public void theClientReceivesStatusCodeOf(int statusCodeValue) {
        context.response()
                .then()
                .statusCode(statusCodeValue);
    }

    @And("errors contains {word}")
    public void errorsContainsName(String field) {
        final var apiError = context.response().getBody().as(ApiError.class);
        assertTrue(apiError.errors().containsKey(field));
    }
}
