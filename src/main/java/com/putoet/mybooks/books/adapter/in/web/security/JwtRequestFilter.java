package com.putoet.mybooks.books.adapter.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    public JwtRequestFilter(UserDetailsService userDetailsService, JwtTokenUtils jwtTokenUtils) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtils = jwtTokenUtils;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getCookies() != null) {
            final Optional<String> jwtToken = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("jwt"))
                    .map(Cookie::getValue)
                    .findFirst();

            if (jwtToken.isPresent()) {
                final String id = jwtTokenUtils.extractUsername(jwtToken.get());
                if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    final UserDetails userDetails = userDetailsService.loadUserByUsername(id);
                    if (jwtTokenUtils.validateToken(jwtToken.get(), userDetails)) {
                        final UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}