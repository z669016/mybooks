package com.putoet.mybooks.books.adapter.in.web.security;

import com.putoet.mybooks.books.application.port.in.security.UserManagementPort;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserManagementPort userManagementPort;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    @PostMapping(path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
        )
    public JwtResponse login(@RequestBody @Valid UserLoginRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.id(), request.password(), new ArrayList<>())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.id());
            if (userDetails != null) {
                final String jwt = jwtTokenUtils.generateToken(userDetails);
                response.addCookie(jwtCookie(jwt));
                response.setHeader(JwtRequestFilter.AUTHORIZATION_KEY, JwtRequestFilter.AUTHORIZATION_SCHEME + " " + jwt);
                return new JwtResponse(jwt, JwtTokenUtils.EXPIRES_IN);
            }

            log.error("No user details for id {}", request.id());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No user details for id " + request.id());
        } catch (DisabledException exc) {
            log.error("User account was disabled for for user {}", request.id());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exc.getMessage());
        } catch (LockedException exc) {
            log.error("User account was locked for user {}", request.id());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exc.getMessage());
        } catch (BadCredentialsException exc) {
            log.error("Invalid userid/password for user {}", request.id());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exc.getMessage());
        }
    }

    private static Cookie jwtCookie(String jwt) {
        final Cookie cookie = new Cookie(JwtRequestFilter.AUTHORIZATION_COOKIE, jwt);
        cookie.setMaxAge(JwtTokenUtils.EXPIRES_IN); // expires in 7 days
        cookie.setHttpOnly(true);
        cookie.setPath("/"); // Global
        return cookie;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/user",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse postUser(@RequestBody @Valid NewUserRequest request) {
        try {
            return UserResponse.from(userManagementPort.registerUser(request.id(),
                    request.name(),
                    request.password(),
                    AccessRole.from(request.accessRole()))
            );
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<UserResponse> getUsers() {
        try {
            return UserResponse.from(userManagementPort.users());
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse getUserById(@PathVariable(name = "id") @Email String id) {
        try {
            final Optional<User> user = userManagementPort.userById(id);
            if (user.isPresent())
                return UserResponse.from(user.get());
        } catch (RuntimeException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
    }
}