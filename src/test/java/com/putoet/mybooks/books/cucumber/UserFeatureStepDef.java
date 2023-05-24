package com.putoet.mybooks.books.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.adapter.in.web.security.*;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        id = translateParameter(id);
        password = translateParameter(password);

        final UserLoginRequest login = new UserLoginRequest(id, password);
        executePost("/login", mapper.writeValueAsString(login));
    }

    @And("response contains a token")
    public void responseContainsAToken() throws JsonProcessingException {
        final JwtResponse response = mapper.readValue(context.response().getBody(), JwtResponse.class);
        assertNotNull(response.access_token());
        assertEquals(JwtRequestFilter.AUTHORIZATION_SCHEME, response.token_type());
    }

    @And("response authorization header contains bearer token")
    public void responseAuthorizationHeaderContainsBearerToken() {
    }

    @And("response cookie jwt is set with token")
    public void responseCookieJwtIsSetWithToken() {
    }

    @When("send a new user request for user with id {string}, name {string}, password {string} and role {string}")
    public void sendANewUserRequestForUserWithIdNamePasswordAndRole(String id, String name, String password, String role) throws JsonProcessingException {
        id = translateParameter(id);
        name = translateParameter(name);
        password = translateParameter(password);
        role = translateParameter(role);

        final NewUserRequest newUserRequest = new NewUserRequest(id, name, password, role);
        executePost("/user", mapper.writeValueAsString(newUserRequest), true);
    }

    @And("user has id {string}")
    public void userHasId(String id) throws JsonProcessingException {
        final UserResponse user = mapper.readValue(context.response().getBody(), UserResponse.class);
        assertEquals(id, user.id());
    }

    @And("user has name {string}")
    public void userHasName(String name) throws JsonProcessingException {
        final UserResponse user = mapper.readValue(context.response().getBody(), UserResponse.class);
        assertEquals(name, user.name());
    }

    @And("user has role {string}")
    public void userHasRole(String role) throws JsonProcessingException {
        final UserResponse user = mapper.readValue(context.response().getBody(), UserResponse.class);
        assertEquals(role, user.accessRole());
    }
}
