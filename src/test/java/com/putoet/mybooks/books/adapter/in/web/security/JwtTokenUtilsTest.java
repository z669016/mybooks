package com.putoet.mybooks.books.adapter.in.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilsTest {
    private static final JwtTokenUtils utils = new JwtTokenUtils();
    private final UserDetails userDetails = new UserDetails() {
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of((GrantedAuthority) () -> "REQUESTER",
                    (GrantedAuthority) () -> "APPROVER"
            );
        }

        @Override
        public String getPassword() {
            return "password";
        }

        @Override
        public String getUsername() {
            return "username";
        }
    };

    private String token;

    @BeforeEach
    void setup() {
        token = utils.generateToken(userDetails);
    }

    @Test
    void extractUsername() {
        assertEquals(userDetails.getUsername(), utils.extractUsername(token));
    }

    @Test
    void extractExpiration() {
        assertTrue( System.currentTimeMillis() - utils.extractExpiration(token).getTime() < 100);
    }

    @Test
    void validateToken() {
        assertTrue(utils.validateToken(token, userDetails.getUsername()));
    }
}