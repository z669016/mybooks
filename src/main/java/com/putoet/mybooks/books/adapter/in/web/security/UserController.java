package com.putoet.mybooks.books.adapter.in.web.security;

import com.putoet.mybooks.books.application.port.security.UserService;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtTokenUtils jwtTokenUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @PostMapping(path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
        )
    public ResponseEntity<String> login(@RequestBody UserRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.id(), request.password(), new ArrayList<>())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.id());
            if (userDetails != null) {
                final String jwt = jwtTokenUtils.generateToken(userDetails);
                final Cookie cookie = new Cookie("jwt", jwt);
                cookie.setMaxAge(7 * 24 * 60 * 60); // expires in 7 days
                cookie.setHttpOnly(true);
                cookie.setPath("/"); // Global
                response.addCookie(cookie);
                return ResponseEntity.ok(jwt);
            }

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authentication error");
        } catch (Exception exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PostMapping(path = "/user",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse postUser(@RequestBody UserRequest request) {
        try {
            return UserResponse.from(userService.registerUser(request.id(),
                    request.name(),
                    request.password(),
                    AccessRole.from(request.accessRole()))
            );
        } catch (SecurityException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserResponse> getUsers() {
        try {
            return UserResponse.from(userService.users());
        } catch (SecurityException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @GetMapping(path = "/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse getUserById(@PathVariable(name = "id") String id) {
        try {
            final Optional<User> user = userService.userById(id);
            if (user.isPresent())
                return UserResponse.from(user.get());
        } catch (SecurityException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
    }
}