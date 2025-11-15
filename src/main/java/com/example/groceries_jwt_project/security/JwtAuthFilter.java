package com.example.groceries_jwt_project.security;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthFilter:
 *  - Runs ONCE per request.
 *  - Reads "Authorization: Bearer <token>" header.
 *  - Validates token using JwtUtil.
 *  - If valid: sets Authentication into SecurityContext (so Spring knows "who").
 *  - The rest of the filter chain then applies authorization rules.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil; // Our utility to generate/parse JWT

    @Autowired
    private MyUserDetailsService userDetailsService; // Loads user + roles from DB

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Skip JWT validation for auth endpoints (login/register) to avoid noise.
        //    (They don't need a token yet.)
        String path = request.getRequestURI();
        if (path.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) Read "Authorization" header. We expect 'Bearer <token>'.
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // cut "Bearer "
                // 3) Extract username; JwtUtil will also verify signature along the way
                username = jwtUtil.extractUsername(token);
            }

            // 4) If we extracted a username and the user is not already authenticated in this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user from DB (grants/roles included)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Basic check: subject in token should match this user.
                // (You can also add extra checks: token not revoked, token not expired, etc.)
                if (username.equals(userDetails.getUsername())) {
                    // 5) Build an Authentication with authorities (roles)
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, // principal
                                    null,        // no credentials stored after auth
                                    userDetails.getAuthorities() // roles/authorities
                            );

                    // 6) Attach request details (IP, session id for audit/logs)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 7) Put the Authentication into the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // (Optional demo logging)
                    System.out.println("[JWT] Token validated for user: " + username +
                            " | roles=" + userDetails.getAuthorities()
                                    .stream().map(Object::toString).collect(Collectors.joining(",")));
                }
            }
        } catch (ExpiredJwtException ex) {
            // Token is valid structurally but has expired
            System.out.println("[JWT] Expired token: " + ex.getMessage());
        } catch (Exception ex) {
            // Signature invalid, malformed token, etc.
            System.out.println("[JWT] Validation failed: " + ex.getMessage());
        }

        // 8) Continue down the chain (either authenticated or not).
        //    If not authenticated and endpoint requires it, Security will return 401/403.
        filterChain.doFilter(request, response);
    }
}
