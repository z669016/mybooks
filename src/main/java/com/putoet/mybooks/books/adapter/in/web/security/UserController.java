package com.putoet.mybooks.books.adapter.in.web.security;

import com.putoet.mybooks.books.application.port.in.security.UserManagementPort;
import com.putoet.mybooks.books.domain.security.AccessRole;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Set;

@RestController
public class UserController {
    public static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserManagementPort userManagementPort;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public UserController(UserManagementPort userManagementPort, AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        this.userManagementPort = userManagementPort;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        log.debug("UserController('{}','{}','{}')", userManagementPort, authenticationManager, userDetailsService);
    }

    @PostMapping(
            path = "/api/v{version}/login",
            version = "1.0",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public JwtResponse login(@Valid @RequestBody UserLoginRequest request, HttpServletResponse response) {
        log.debug("login('{}', '***')", request.id());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.id(), request.password(), new ArrayList<>())
            );

            final var userDetails = userDetailsService.loadUserByUsername(request.id());
            final String jwt = JwtTokenUtils.generateToken(userDetails);
            response.addCookie(jwtCookie(jwt));
            response.setHeader(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + jwt);
            final var jwtResponse = new JwtResponse(jwt, JwtTokenUtils.EXPIRES_IN);

            log.debug("login returns: {}", jwtResponse);
            return jwtResponse;
        } catch (DisabledException exc) {
            log.error("User account was disabled for for user {}", request.id());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exc.getMessage());
        } catch (LockedException exc) {
            log.error("User account was locked for user {}", request.id());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exc.getMessage());
        } catch (BadCredentialsException | UsernameNotFoundException exc) {
            log.error("Invalid userid/password for user {}", request.id());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exc.getMessage());
        }
    }

    private static Cookie jwtCookie(String jwt) {
        final var cookie = new Cookie(JwtRequestFilter.AUTHORIZATION_COOKIE, jwt);
        cookie.setMaxAge(JwtTokenUtils.EXPIRES_IN); // expires in 7 days
        cookie.setHttpOnly(true);
        cookie.setPath("/"); // Global
        cookie.setSecure(true);
        return cookie;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            path = "/api/v{version}/user",
            version = "1.0",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody @Valid NewUserRequest request) {
        log.debug("createUser('{}', '{}', '***', '{}')", request.id(), request.name(), request.accessRole());

        try {
            final var response = UserResponse.from(userManagementPort.registerUser(request.id(),
                    request.name(),
                    request.password(),
                    AccessRole.from(request.accessRole()))
            );
            log.debug("create user returns: {}", response);
            return response;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(
            path = "/api/v{version}/users",
            version = "1.0",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Set<UserResponse> getUsers() {
        log.debug("getUsers()");
        try {
            final var users = UserResponse.from(userManagementPort.users());
            log.debug("get users returns: {}", users);
            return users;
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(
            path = "/api/v{version}/user/{id}",
            version = "1.0",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse getUserById(@PathVariable(name = "id") @Email String id) {
        log.debug("getUserById('{}')", id);
        try {
            final var user = userManagementPort.userById(id);
            if (user.isPresent()) {
                log.debug("get user by id returns:{}", user);
                return UserResponse.from(user.get());
            }
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
    }
}