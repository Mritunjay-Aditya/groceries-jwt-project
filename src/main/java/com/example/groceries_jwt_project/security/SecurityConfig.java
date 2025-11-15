package com.example.groceries_jwt_project.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig: Central place to define HOW requests are authenticated and authorized.
 *
 * Big picture for JWT apps:
 * 1) We authenticate once at /auth/login and issue a signed token.
 * 2) For every subsequent request, the client sends "Authorization: Bearer <token>".
 * 3) Our JwtAuthFilter validates that token and sets Authentication in the SecurityContext.
 * 4) Based on the Authentication (user + roles), Spring matches authorization rules below.
 */
@Configuration                     // Marks this as a Spring @Configuration class (creates beans)
@EnableWebSecurity                  // Enables Spring Security’s web stack
@EnableMethodSecurity               // Allows @PreAuthorize / @Secured on controllers/services
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtFilter;           // Our custom filter that reads/validates the JWT

    @Autowired
    private MyUserDetailsService userService;  // Loads users from DB for authentication

    // 1) Password encoder:
    //    - BCrypt is the standard for hashing passwords before storing in DB.
    //    - When a user logs in, Spring compares raw password with hashed (BCrypt matches).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2) Authentication provider:
    //    - Tells Spring *how* to fetch users and *how* to verify their passwords.
    //    - We wire MyUserDetailsService + BCrypt into a DaoAuthenticationProvider.
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);   // Who provides UserDetails? (from DB)
        provider.setPasswordEncoder(passwordEncoder()); // How to verify password? (BCrypt)
        return provider;
    }

    // 3) AuthenticationManager:
    //    - The engine that performs authentication (e.g., on /auth/login).
    //    - We obtain the prebuilt manager from Spring’s AuthenticationConfiguration.
    @Bean
    public AuthenticationManager 
    authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 4) Security filter chain:
    //    - The pipeline that processes EVERY HTTP request.
    //    - We disable CSRF (stateless API), make sessions STATELESS (JWT carries auth),
    //      declare which URLs are public vs. protected, and insert our JwtAuthFilter.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF is primarily for browser sessions with cookies.
            // For stateless token-based APIs, we turn it off.
            .csrf(csrf -> csrf.disable())

            // JWT apps do not use server-side HTTP sessions to remember who you are.
            // Every request must carry a valid token, so make the session STATELESS.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules (WHO can access WHICH endpoints):
            // Order matters: the first match wins.
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no token needed): allow registration + login
                //.requestMatchers("/auth/**").permitAll()
                // --- Swagger / OpenAPI WHITELIST (no JWT needed) ---
                .requestMatchers(
                	"/auth/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**"
                ).permitAll()


                // Optional: make product GETs public for demo/catalog browsing
                .requestMatchers(HttpMethod.GET, "/api/groceries/**").permitAll()

                // Write operations on products require a user with ROLE_ADMIN.
                // NOTE: Your UserDetails must expose "ROLE_ADMIN" (with ROLE_ prefix)
                .requestMatchers(HttpMethod.POST, "/api/groceries/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/groceries/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/groceries/**").hasRole("ADMIN")

                // Everything else requires the user to be authenticated (valid JWT)
                .anyRequest().authenticated()
            )

            // Tell Spring to use our DB-backed authentication (DaoAuthenticationProvider)
            .authenticationProvider(authProvider())

            // Insert our JWT filter BEFORE UsernamePasswordAuthenticationFilter:
            // - UsernamePasswordAuthenticationFilter handles form login.
            // - We need to set Authentication from the token earlier in the chain.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // Finally return the built filter chain to Spring Security
        return http.build();
    }
}

//401 vs 403:
//401 Unauthorized = token missing/invalid (no Authentication).
//403 Forbidden = token valid, user authenticated, but not authorized for the endpoint/role.
//
//Roles: Spring expects ROLE_ prefix internally. If you store "ROLE_ADMIN" in DB, 
//use .hasRole("ADMIN").
//
//
//Testing:
//POST /auth/register → create user (ROLE_ADMIN for admin ops).
//POST /auth/login → get token string.
//Use token in header: Authorization: Bearer <token> to call /api/products.