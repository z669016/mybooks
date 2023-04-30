package com.putoet.mybooks.books.adapter.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class JwtRequestFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_KEY = "Authorization";
    public static final String AUTHORIZATION_SCHEME = "Bearer";
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    public JwtRequestFilter(UserDetailsService userDetailsService, JwtTokenUtils jwtTokenUtils) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Optional<String> jwtToken = Optional.empty();

        if (request.getCookies() != null) {
            jwtToken = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals(AUTHORIZATION_KEY))
                    .map(Cookie::getValue)
                    .findFirst();
        }

        if (jwtToken.isEmpty()) {
            final String header = request.getHeader(AUTHORIZATION_KEY);
            if (header != null && header.toLowerCase().startsWith(AUTHORIZATION_SCHEME.toLowerCase() + " ")) {
                jwtToken = Optional.of(request.getHeader(AUTHORIZATION_KEY).substring(AUTHORIZATION_SCHEME.length() + 1));
            }
        }

        if (jwtToken.isPresent()) {
            final String id = jwtTokenUtils.extractUsername(jwtToken.get());
            if (id != null) {
                if (SecurityContextHolder.getContext().getAuthentication() != null &&
                    !id.equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
                    SecurityContextHolder.getContext().setAuthentication(null);
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    final UserDetails userDetails = userDetailsService.loadUserByUsername(id);
                    if (isActiveUser(userDetails) && jwtTokenUtils.validateToken(jwtToken.get(), userDetails.getUsername())) {
                        final UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(id, null, jwtTokenUtils.extractAuthorities(jwtToken.get()));
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
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