package com.putoet.mybooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.adapter.in.web.security.JwtRequestFilter;
import com.putoet.mybooks.books.adapter.in.web.security.JwtResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MybooksApplication.class)
@AutoConfigureMockMvc
class MybooksApplicationTest {

    @Autowired
    private MockMvc mvc;

    private String adminToken;
    private String userToken;

    @BeforeEach
     void init() throws Exception {
        if (adminToken != null || userToken != null)
            return;

        JwtResponse jwtResponse = loginFor("z669016@gmail.com", "1password!");
        adminToken = jwtResponse.access_token();

        jwtResponse = loginFor("putoet@outlook.com", "2password!");
        userToken = jwtResponse.access_token();
    }

    private JwtResponse loginFor(String id, String password) throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();

        var result = mvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .content(asJsonString(Map.of("id", id, "password", password)))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void users() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(JwtRequestFilter.AUTHORIZATION_KEY, "BEARER" + " " + adminToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    void usersFailed() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + userToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void authorsByAdmin() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + adminToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    void authorsByUser() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + adminToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    void authorFailed() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/author/  ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + adminToken)
                )
                .andExpect(status().isBadRequest());
    }
}
