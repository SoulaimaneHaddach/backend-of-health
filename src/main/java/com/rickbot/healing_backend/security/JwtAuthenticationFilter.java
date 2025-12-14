package com.rickbot.healing_backend.security;


import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final DoctorServiceDetails doctorDetailsService;
    private final com.rickbot.healing_backend.repository.AdminRepository adminRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token != null && jwtUtil.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtUtil.getEmailFromToken(token);
            org.springframework.security.core.userdetails.UserDetails userDetails = null;
            try {
                userDetails = (org.springframework.security.core.userdetails.UserDetails) doctorDetailsService.loadUserByUsername(email);
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                // Not a doctor — try loading admin from DB
                var adminOpt = adminRepository.findByEmail(email);
                if (adminOpt.isPresent()) {
                    var a = adminOpt.get();
                    if (a.isEnabled() && a.getRoles() != null) {
                        var auths = a.getRoles().stream()
                                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r))
                                .toList();
                        userDetails = org.springframework.security.core.userdetails.User.withUsername(a.getEmail())
                                .password(a.getPassword() == null ? "" : a.getPassword())
                                .authorities(auths)
                                .accountExpired(false)
                                .accountLocked(!a.isEnabled())
                                .credentialsExpired(false)
                                .disabled(!a.isEnabled())
                                .build();
                    }
                } else {
                    // fallback: check token roles and create minimal admin principal if token contains admin role
                        var roles = jwtUtil.getRolesFromToken(token);
                        boolean hasAdmin = roles.stream().anyMatch(r -> r != null && (r.equalsIgnoreCase("ROLE_ADMIN") || r.equalsIgnoreCase("ADMIN")));
                        if (hasAdmin) {
                            // normalize authorities: ensure Spring-style ROLE_ prefix
                            var auths = roles.stream()
                                    .filter(r -> r != null)
                                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                                    .toList();
                            userDetails = org.springframework.security.core.userdetails.User.withUsername(email)
                                    .password("")
                                    .authorities(auths)
                                    .build();
                        }
                }
            }

            if (userDetails != null) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}

