package com.putoet.mybooks.books.adapter.in.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.MybooksApplication;
import com.putoet.mybooks.books.adapter.in.web.MockRequest;
import com.putoet.mybooks.books.adapter.in.web.security.validation.AccessRoleConstraint;
import com.putoet.mybooks.books.adapter.in.web.security.validation.PasswordConstraint;
import com.putoet.mybooks.books.domain.validation.StandardValidations;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MybooksApplication.class)
@AutoConfigureMockMvc
class UserControllerE2ETest {

    @Autowired
    private MockMvc mvc;

    private MockRequest mockRequest;
    private String adminToken;
    private ObjectMapper mapper;

    @BeforeEach
    void init() throws Exception {
        mockRequest = new MockRequest(mvc);
        mapper = new ObjectMapper();
        adminToken = mockRequest.adminToken();
    }

    @Test
    void login() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(MockRequest.userLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", Matchers.notNullValue()));
    }

    @Test
    void loginFails() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserLoginRequest(null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value(PasswordConstraint.PASSWORD_ERROR));

        mvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserLoginRequest(null, "1password!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").value(StandardValidations.message(NotNull.class)));

        mvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserLoginRequest("bla", "1password!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").value(StandardValidations.message(Email.class)))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
    }

    @Test
    void postUser() throws Exception {
        final NewUserRequest newUserRequest = new NewUserRequest("z669016@ziggo.nl", "Ik", "3password@", "user");
        final String body = mvc.perform(mockRequest.jwtPostRequestWithToken("/user", adminToken, mapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final UserResponse userResponse = mapper.readValue(body, UserResponse.class);
        assertAll(
                () -> assertEquals(newUserRequest.id(), userResponse.id()),
                () -> assertEquals(newUserRequest.name(), userResponse.name()),
                () -> assertEquals(newUserRequest.accessRole().toLowerCase(), userResponse.accessRole().toLowerCase())
        );
    }

    @Test
    void postUserFails() throws Exception {
        NewUserRequest newUserRequest = new NewUserRequest(null, null, null, null);
        mvc.perform(mockRequest.jwtPostRequestWithToken("/user", adminToken, mapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value(PasswordConstraint.PASSWORD_ERROR))
                .andExpect(jsonPath("$.errors.accessRole").value(AccessRoleConstraint.ACCESS_ROLE_ERROR))
                .andExpect(jsonPath("$.errors.name").value(StandardValidations.message(NotBlank.class)))
                .andExpect(jsonPath("$.errors.id").value(StandardValidations.message(NotNull.class)))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
    }
}
