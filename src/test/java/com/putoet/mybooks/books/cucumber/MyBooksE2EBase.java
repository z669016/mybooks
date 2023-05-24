package com.putoet.mybooks.books.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.MybooksApplication;
import com.putoet.mybooks.books.adapter.in.web.security.JwtResponse;
import com.putoet.mybooks.books.adapter.in.web.security.UserLoginRequest;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CucumberContextConfiguration
@SpringBootTest(classes = MybooksApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MyBooksE2EBase {
    private static final String URL_PREFIX = "https://localhost:443";

    protected final TestContext context;
    protected final ObjectMapper mapper;
    protected final RestTemplate sslRestTemplate;

    public MyBooksE2EBase(RestTemplate sslRestTemplate, ObjectMapper mapper) {
        this.sslRestTemplate = sslRestTemplate;
        this.mapper = mapper;
        context = TestContext.getInstance();
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

    protected void executePost(String url, String json) {
        executePost(url, json, false);
    }

    protected void executePost(String url, String json, boolean includeToken) {
        final HttpHeaders headers = httpHeaders(includeToken);
        final HttpEntity<String> entity = new HttpEntity<>(json, headers);

        context.response(executeHttpCall(() -> sslRestTemplate.postForEntity(URL_PREFIX + url, entity, String.class)));
    }

    protected void executeGet(String url, String json) {
        executeGet(url, json, false);
    }

    protected void executeGet(String url, String json, boolean includeToken) {
        final HttpHeaders headers = httpHeaders(true);
        final HttpEntity<String> entity = new HttpEntity<>(json, headers);

        context.response(executeHttpCall(() -> sslRestTemplate.exchange(URL_PREFIX + url, HttpMethod.GET, entity, String.class)));
    }

    protected void executePut(String url, String json) {
        executePut(url, json, false);
    }

    protected void executePut(String url, String json, boolean includeToken) {
        final HttpHeaders headers = httpHeaders(includeToken);
        final HttpEntity<String> entity = new HttpEntity<>(json, headers);

        context.response(executeHttpCall(() -> sslRestTemplate.exchange(URL_PREFIX + url, HttpMethod.PUT, entity, String.class)));
    }

    private ResponseEntity<String> executeHttpCall(Supplier<ResponseEntity<String>> supplier) {
        try {
            return supplier.get();
        } catch (HttpStatusCodeException exc) {
            return ResponseEntity
                    .status(exc.getStatusCode())
                    .headers(exc.getResponseHeaders())
                    .body(exc.getResponseBodyAsString());
        }
    }

    private HttpHeaders httpHeaders() {
        return httpHeaders(false);
    }

    private HttpHeaders httpHeaders(boolean includeToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (includeToken && context.token() != null) {
            headers.setBearerAuth(context.token());
        }

        return headers;
    }

    protected String translateParameter(String param) {
        return switch (param) {
            case "null" -> null;
            default -> param;
        };
    }
}