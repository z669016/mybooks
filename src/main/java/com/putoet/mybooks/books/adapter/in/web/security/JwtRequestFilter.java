package com.putoet.mybooks.books.adapter.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_COOKIE = "jwt";
    public static final String AUTHORIZATION_KEY = "Authorization";
    public static final String AUTHORIZATION_SCHEME = "Bearer";

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var jwtToken = Optional.<String>empty();
        if (request.getCookies() != null) {
            jwtToken = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals(AUTHORIZATION_COOKIE))
                    .map(Cookie::getValue)
                    .findFirst();
            if (jwtToken.isPresent())
                log.info("Found JWT in cookie {}", AUTHORIZATION_COOKIE);
        }

        if (jwtToken.isEmpty()) {
            final String header = request.getHeader(AUTHORIZATION_KEY);
            if (header != null && header.toLowerCase().startsWith(AUTHORIZATION_SCHEME.toLowerCase() + " ")) {
                jwtToken = Optional.of(request.getHeader(AUTHORIZATION_KEY).substring(AUTHORIZATION_SCHEME.length() + 1));
            }
            if (jwtToken.isPresent())
                log.info("Found JWT in header {} with scheme {}", AUTHORIZATION_KEY, AUTHORIZATION_SCHEME);
        }

        if (jwtToken.isPresent()) {
            final String id = jwtTokenUtils.extractUsername(jwtToken.get());
            if (id != null) {
                if (SecurityContextHolder.getContext().getAuthentication() != null &&
                    !id.equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {

                    log.info("Reset security context for user {} to user {}", SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id);
                    SecurityContextHolder.getContext().setAuthentication(null);
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    final var userDetails = userDetailsService.loadUserByUsername(id);
                    if (isActiveUser(userDetails) && jwtTokenUtils.validateToken(jwtToken.get(), userDetails.getUsername())) {
                        final var authenticationToken =
                                new UsernamePasswordAuthenticationToken(id, null, jwtTokenUtils.extractAuthorities(jwtToken.get()));
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        log.info("Set security context to {}", authenticationToken);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isActiveUser(UserDetails userDetails) {
        return userDetails.isAccountNonExpired() &&
               userDetails.isAccountNonLocked() &&
               userDetails.isEnabled() &&
               userDetails.isCredentialsNonExpired();
    }
}