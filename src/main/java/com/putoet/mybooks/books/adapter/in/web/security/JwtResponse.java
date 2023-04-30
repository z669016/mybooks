package com.putoet.mybooks.books.adapter.in.web.security;

public record JwtResponse(String access_token, String token_type, int expires_in) {
    public JwtResponse(String access_token, int expires_in) {
        this(access_token, JwtRequestFilter.AUTHORIZATION_SCHEME, expires_in);
    }
}
