package com.putoet.mybooks.books.cucumber;

import com.putoet.mybooks.books.adapter.in.web.security.*;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserFeatureStepDef extends MyBooksE2EBase {
    @Before
    public void setup() {
        context.clear();
    }

    @When("send a login request for user {string} with password {string}")
    public void sendALoginRequestForUserWithPassword(String id, String password) {
        id = translateParameter(id);
        password = translateParameter(password);

        final var login = new UserLoginRequest(id, password);
        executePost("/login", login, false);
    }

    @And("response contains a token")
    public void responseContainsAToken() {
        context.response()
                .then()
                .assertThat()
                .body("access_token", is(not(emptyString())))
                .body("token_type", equalTo(JwtRequestFilter.AUTHORIZATION_SCHEME));
    }

    @And("response authorization header contains bearer token")
    public void responseAuthorizationHeaderContainsBearerToken() {
        final var response = context.response().body().as(JwtResponse.class);
        final var header = context.response().header(JwtRequestFilter.AUTHORIZATION_KEY);
        assertNotNull(header);
        assertTrue(header.startsWith(JwtRequestFilter.AUTHORIZATION_SCHEME + " "));
        assertEquals(response.access_token(), header.substring(JwtRequestFilter.AUTHORIZATION_SCHEME.length() + 1));
    }

    @And("response cookie jwt is set with token")
    public void responseCookieJwtIsSetWithToken() {
        final var response = context.response().body().as(JwtResponse.class);
        final var header = context.response().header(HttpHeaders.SET_COOKIE);
        assertNotNull(header);
        assertTrue(header.startsWith(JwtRequestFilter.AUTHORIZATION_COOKIE + "="));

        final var cookie = header.split("; ");
        assertEquals(5, cookie.length);
        assertEquals(response.access_token(), cookie[0].substring(JwtRequestFilter.AUTHORIZATION_COOKIE.length() + 1));
        assertEquals("Max-Age=3600", cookie[1]);
        assertTrue(cookie[2].startsWith("Expires="));
        assertEquals("Path=/", cookie[3]);
        assertEquals("HttpOnly", cookie[4]);
    }

    @When("send a new user request for user with id {string}, name {string}, password {string} and role {string}")
    public void sendANewUserRequestForUserWithIdNamePasswordAndRole(String id, String name, String password, String role) {
        id = translateParameter(id);
        name = translateParameter(name);
        password = translateParameter(password);
        role = translateParameter(role);

        final var newUserRequest = new NewUserRequest(id, name, password, role);
        executePost("/user", newUserRequest, true);
    }

    @And("user has id {string}")
    public void userHasId(String id) {
        context.response()
                .then()
                .assertThat()
                .body("id", equalTo(id));
    }

    @And("user has name {string}")
    public void userHasName(String name) {
        context.response()
                .then()
                .assertThat()
                .body("name", equalTo(name));
    }

    @And("user has role {string}")
    public void userHasRole(String role) {
        context.response()
                .then()
                .assertThat()
                .body("accessRole", equalTo(role));
    }

    @When("send a get request for users")
    public void sendAGetRequestForUsers() {
        executeGet("/users");
    }

    @And("response contains details on more than {int} user")
    public void responseContainsDetailsOnMoreThanUser(int moreThanCountUsers) {
        final var users = context.response().body().jsonPath().getList(".", UserResponse.class);
        assertTrue(users.size() > moreThanCountUsers);
    }
}
