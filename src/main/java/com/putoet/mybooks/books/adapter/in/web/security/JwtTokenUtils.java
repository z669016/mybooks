package com.putoet.mybooks.books.adapter.in.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public final class JwtTokenUtils {
    private static final String SECRET_KEY = "FlorisEmmaHannesPoppieLevelAlHeelLangSamenBijOnsInHuis";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    public static final String AUTHORITIES_KEY = "authorities";
    public static final int EXPIRES_IN = 60 * 60;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        return toGrantedAuthoritiesCollection(extractAllClaims(token).get(AUTHORITIES_KEY, String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final var claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(SIGNING_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        final var claims = new HashMap<String, Object>();
        claims.put(AUTHORITIES_KEY, fromGrantedAuthoritiesCollection(userDetails.getAuthorities()));
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        final long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + 1000 * EXPIRES_IN))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();
    }

    public Boolean validateToken(String token, String id) {
        final String username = extractUsername(token);
        return (username.equals(id) && !isTokenExpired(token));
    }

    private String fromGrantedAuthoritiesCollection(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));
    }

    private Collection<? extends GrantedAuthority> toGrantedAuthoritiesCollection(String authorities) {
        return Arrays.stream(authorities.split(" "))
                .map(authority -> new SimpleGrantedAuthority(authorities))
                .toList();
    }
}