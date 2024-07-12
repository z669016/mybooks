package com.putoet.mybooks.books.adapter.in.web.security;

import com.putoet.mybooks.books.application.security.UserService;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {
    private final JwtTokenUtils utils = new JwtTokenUtils();
    private final UserLoginRequest loginRequest = new UserLoginRequest("abc@xyz.com", "pwd");
    private final NewUserRequest request = new NewUserRequest("abc@xyz.com", "name", "pwd", "ADMIN");
    private final UserDetails userDetails = new UserDetails() {
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of((GrantedAuthority) () -> "ADMIN");
        }

        @Override
        public String getPassword() {
            return request.password();
        }

        @Override
        public String getUsername() {
            return request.id();
        }
    };

    private UserService userService;
    private UserDetailsService userDetailService;
    private AuthenticationManager authenticationManager;
    private UserController userController;

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        userDetailService = mock(UserDetailsService.class);
        authenticationManager = mock(AuthenticationManager.class);
        userController = new UserController(userService, authenticationManager, userDetailService, utils);
    }

    @Test
    void login() {
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(userDetailService.loadUserByUsername(loginRequest.id())).thenReturn(userDetails);
        final JwtResponse result = userController.login(loginRequest, response);

        assertAll(
                () -> verify(authenticationManager, times(1)).authenticate(any()),
                () -> verify(response, times(1)).addCookie(any()),
                () -> assertNotNull(result.access_token())
        );
    }

    @Test
    void loginFailed() {
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(userDetailService.loadUserByUsername(loginRequest.id())).thenReturn(null);
        try {
            userController.login(loginRequest, response);
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertAll(
                    () -> assertEquals(HttpStatus.FORBIDDEN, exc.getStatusCode()),
                    () -> verify(authenticationManager, times(1)).authenticate(any()),
                    () -> verify(response, times(0)).addCookie(any())
            );
        }
    }
    @Test
    void loginInvalidCredentials() {
        authenticateError(new BadCredentialsException("credentials"));
    }

    @Test
    void loginDisabled() {
        authenticateError(new DisabledException("disabled"));
    }

    @Test
    void loginLocked() {
        authenticateError(new LockedException("locked"));
    }

    private <T extends AuthenticationException> void authenticateError(T exception) {
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(authenticationManager.authenticate(any())).thenThrow(exception);
        try {
            userController.login(loginRequest, response);
            fail("ResponseStatusException expected");
        } catch (RuntimeException exc) {
            if (exc instanceof ResponseStatusException rsa) {
                assertEquals(HttpStatus.FORBIDDEN, rsa.getStatusCode());
            } else {
                fail("Expected ResponseStatusException but caught " + exc.getClass().getName());
            }
        }
    }

    @Test
    void postUser() {
        when(userService.registerUser(request.id(), request.name(), request.password(), AccessRole.from(request.accessRole())))
                .thenReturn(new User(request.id(), request.name(), request.password(), AccessRole.from(request.accessRole())));
        final UserResponse user = userController.postUser(request);

        assertAll(
                () -> verify(userService, times(1)).registerUser(request.id(), request.name(), request.password(), AccessRole.from(request.accessRole())),
                () -> assertEquals(request.id(), user.id()),
                () -> assertEquals(request.name(), user.name()),
                () -> assertEquals(request.accessRole(), user.accessRole())
        );
    }

    @Test
    void postUserFailed() {
        when(userService.registerUser(request.id(), request.name(), request.password(), AccessRole.from(request.accessRole()))).thenThrow(new IllegalStateException("ERROR"));
        try {
            userController.postUser(request);
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void getUsers() {
        when(userService.users()).thenReturn(Set.of(new User(request.id(), request.name(), request.password(), AccessRole.from(request.accessRole()))));
        final Set<UserResponse> users = userController.getUsers();

        assertAll(
                () -> verify(userService, times(1)).users(),
                () -> assertEquals(1, users.size())
        );
    }

    @Test
    void getUsersFailed() {
        when(userService.users()).thenThrow(new IllegalStateException("ERROR"));
        try {
            userController.getUsers();
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }

    @Test
    void getUserById() {
        when(userService.userById(request.id())).thenReturn(Optional.of(new User(request.id(), request.name(), request.password(), AccessRole.from(request.accessRole()))));
        final UserResponse response = userController.getUserById(request.id());

        assertAll(
                () -> verify(userService, times(1)).userById(request.id()),
                () -> assertEquals(request.id(), response.id()),
                () -> assertEquals(request.name(), response.name()),
                () -> assertEquals(request.accessRole(), response.accessRole())
        );
    }

    @Test
    void getUserByIdNotFound() {
        when(userService.userById(request.id())).thenReturn(Optional.empty());
        try {
            userController.getUserById(request.id());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertAll(
                    () -> assertEquals(HttpStatus.NOT_FOUND, exc.getStatusCode()),
                    () -> verify(userService, times(1)).userById(request.id())
            );
        }
    }

    @Test
    void getUserByIdFailed() {
        when(userService.userById(request.id())).thenThrow(new IllegalStateException("ERROR"));
        try {
            userController.getUserById(request.id());
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException exc) {
            assertEquals(HttpStatus.BAD_REQUEST, exc.getStatusCode());
        }
    }
}