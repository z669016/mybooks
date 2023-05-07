package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.adapter.in.web.security.JwtRequestFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class MockRequest {
    public static MockHttpServletRequestBuilder jwtGetRequestWithToken(String url, String token) {
        return MockMvcRequestBuilders
                .get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + token);
    }

    public static MockHttpServletRequestBuilder jwtPostRequestWithToken(String url, String token, String json) {
        return MockMvcRequestBuilders
                .post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + token)
                .content(json);
    }

    public static MockHttpServletRequestBuilder jwtPutRequestWithToken(String url, String token, String json) {
        return MockMvcRequestBuilders
                .put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + token)
                .content(json);
    }
}
