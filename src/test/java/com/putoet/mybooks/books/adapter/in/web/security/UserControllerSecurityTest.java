package com.putoet.mybooks.books.adapter.in.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoet.mybooks.books.application.security.UserService;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {
    // Mocking the services is necessary to prevent the Spring context from loading the actual beans.
    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailService;

    @MockBean
    private AuthenticationManager authenticationManager;

    // Mocking the DataSourceScriptDatabaseInitializer is required to prevent the database gets recreated and
    // the data gets reloaded. This is probably a work-around, and I'm probably doing something wrong elsewhere
    @MockBean
    private DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginUnAuthenticated() throws Exception {
        performLogin();
    }

    private void performLogin() throws Exception {
        final var login = new UserLoginRequest("email@google.com", "password");
        final UserDetails userDetails = getUserDetails(login, "USER");

        when(userDetailService.loadUserByUsername(login.id())).thenReturn(userDetails);
        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void loginAuthenticated() throws Exception {
        performLogin();
    }

    private static UserDetails getUserDetails(UserLoginRequest login, String role) {
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of((GrantedAuthority) () -> role);
            }

            @Override
            public String getPassword() {
                return login.password();
            }

            @Override
            public String getUsername() {
                return login.id();
            }
        };
    }

    @Test
    void createUserUnAuthenticated() throws Exception {
        mvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getNewUserRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void createUserWronglyAuthenticated() throws Exception {
        mvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getNewUserRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void createUserAuthenticated() throws Exception {
        final var newUserRequest = getNewUserRequest();
        when(userService.registerUser(newUserRequest.id(), newUserRequest.name(), newUserRequest.password(),
                AccessRole.valueOf(newUserRequest.accessRole())))
                .thenReturn(new User(newUserRequest.id(), newUserRequest.name(), newUserRequest.password(),
                        AccessRole.valueOf(newUserRequest.accessRole())));
        mvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated());
    }

    private static NewUserRequest getNewUserRequest() {
        return new NewUserRequest("some@gmail.com", "some", "1password!", "USER");
    }

    @Test
    void getUsersUnAuthenticated() throws Exception {
        mvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void getUsersWronglyAuthenticated() throws Exception {
        mvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void getUsersAuthenticated() throws Exception {
        mvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUserByIdUnAuthenticated() throws Exception {
        mvc.perform(get("/user/some@gmail.com").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void getUserByIdWronglyAuthenticated() throws Exception {
        mvc.perform(get("/user/some@gmail.com").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void getUserByIdAuthenticated() throws Exception {
        when(userService.userById("some@gmail.com")).thenReturn(Optional.empty());
        mvc.perform(get("/user/some@gmail.com").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}