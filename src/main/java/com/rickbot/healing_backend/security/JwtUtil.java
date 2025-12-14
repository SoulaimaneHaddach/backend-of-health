package com.rickbot.healing_backend.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

@Component
public class JwtUtil {

    private final String jwtSecret;
    private final long jwtExpirationMs;
    private final Key signingKey;

    public JwtUtil(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration}") long jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(DoctorPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(principal.getUsername())
                .claim("role", principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    // Generate a token for a subject with explicit roles (e.g. admin)
    public String generateToken(String subject, java.util.Collection<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(subject)
                .claim("role", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getRolesFromToken(String token) {
        try {
            var claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
            Object roles = claims.get("role");
            if (roles instanceof java.util.List) {
                return (java.util.List<String>) roles;
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // ignore and return empty list
        }
        return java.util.List.of();
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getPermissionsFromToken(String token) {
        try {
            var claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
            Object perms = claims.get("perms");
            if (perms instanceof java.util.List) {
                return (java.util.List<String>) perms;
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // ignore and return empty list
        }
        return java.util.List.of();
    }

    public String generateTokenWithPermissions(String subject, java.util.Collection<String> roles, java.util.Collection<String> perms) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(subject)
                .claim("role", roles)
                .claim("perms", perms)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // log if you want
            return false;
        }
    }

    public long getJwtExpirationMs() { return jwtExpirationMs; }
}

