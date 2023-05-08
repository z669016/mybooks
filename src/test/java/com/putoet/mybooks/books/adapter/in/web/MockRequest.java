package com.putoet.mybooks.books.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.adapter.in.web.security.JwtRequestFilter;
import com.putoet.mybooks.books.adapter.in.web.security.JwtResponse;

import com.putoet.mybooks.books.adapter.in.web.security.UserLoginRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MockRequest {
    public static final UserLoginRequest adminLogin = new UserLoginRequest("z669016@gmail.com", "1password!");
    public static final UserLoginRequest userLogin = new UserLoginRequest("putoet@outlook.com", "2password!");

    private final MockMvc mvc;

    private String adminToken;
    private String userToken;

    public MockRequest(MockMvc mvc) {
        this.mvc = mvc;
    }

    public MockHttpServletRequestBuilder jwtGetRequestWithToken(String url, String token) {
        return MockMvcRequestBuilders
                .get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + token);
    }

    public MockHttpServletRequestBuilder jwtPostRequestWithToken(String url, String token, String json) {
        return MockMvcRequestBuilders
                .post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + token)
                .content(json);
    }

    public MockHttpServletRequestBuilder jwtPutRequestWithToken(String url, String token, String json) {
        return MockMvcRequestBuilders
                .put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + token)
                .content(json);
    }

    public String adminToken() throws Exception {
        if (adminToken == null) {
            final JwtResponse jwtResponse = loginFor(adminLogin);
            adminToken = jwtResponse.access_token();
        }

        return adminToken;
    }

    public String userToken() throws Exception {
        if (userToken == null) {
            final JwtResponse jwtResponse = loginFor(userLogin);
            userToken = jwtResponse.access_token();
        }

        return userToken;
    }

    private JwtResponse loginFor(UserLoginRequest loginRequest) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();

        final String json = mvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .content(mapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return mapper.readValue(json, JwtResponse.class);
    }
}
