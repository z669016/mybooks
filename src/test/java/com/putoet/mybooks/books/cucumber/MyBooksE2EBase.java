package com.putoet.mybooks.books.cucumber;

import com.putoet.mybooks.MybooksApplication;
import com.putoet.mybooks.books.adapter.in.web.security.JwtRequestFilter;
import com.putoet.mybooks.books.adapter.in.web.security.JwtResponse;
import com.putoet.mybooks.books.adapter.in.web.security.UserLoginRequest;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CucumberContextConfiguration
@SpringBootTest(classes = MybooksApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MyBooksE2EBase {
    private static final String URL_PREFIX = "https://localhost:443";

    protected final TestContext context = TestContext.getInstance();

    public void userLogin() {
        final UserLoginRequest login = new UserLoginRequest("putoet@outlook.com", "2password!");
        executePost("/login", login, false);
        context.response().then().statusCode(HttpStatus.OK.value());

        final JwtResponse jwtResponse = context.response().body().as(JwtResponse.class);
        assertNotNull(jwtResponse.access_token());
        context.token(jwtResponse.access_token());
    }

    public void adminLogin() {
        final UserLoginRequest login = new UserLoginRequest("z669016@gmail.com", "1password!");
        executePost("/login", login, false);
        context.response().then().statusCode(HttpStatus.OK.value());

        final JwtResponse jwtResponse = context.response().body().as(JwtResponse.class);
        assertNotNull(jwtResponse.access_token());
        context.token(jwtResponse.access_token());
    }

    protected void executePost(String url, Object json) {
        executePost(url, json, true);
    }

    protected void executePost(String url, Object json, boolean includeToken) {
        context.response(request(includeToken).body(json).post(URL_PREFIX + url));
    }

    protected void executeGet(String url) {
        executeGet(url, "");
    }

    protected void executeGet(String url, Object json) {
        executeGet(url, json, true);
    }

    protected void executeGet(String url, Object json, boolean includeToken) {
        context.response(request(includeToken).body(json).get(URL_PREFIX + url));
    }

    protected void executePut(String url, Object json) {
        executePut(url, json, true);
    }

    protected void executePut(String url, Object json, boolean includeToken) {
        context.response(request(includeToken).body(json).put(URL_PREFIX + url));
    }

    protected String translateParameter(String param) {
        return "null".equals(param) ? null : param;
    }

    protected RequestSpecification request(boolean includeToken) {
        final var request = given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON);
        return includeToken ?
                request.header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + context.token()).when() :
                request.when();
    }
}