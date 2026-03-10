/**
 * Security configuration for the application
 * 
 * How it works:
 * - Spring Security intercepts the incoming request
 * - Firstly it checks CORS configuration to allow or deny the request.
 * - Security context is populated with the JwtAuthFilter
 * - Authorization rules decide whether the requests can be processed or needs to be authenticated
 *  - If authentication is required, the AuthenticationManager verifies the token
 * - Password encoder is used to hash and verify passwords, before saving them.
 */

package com.globalyofi.backend.config;

import com.globalyofi.backend.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Main security configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with the following configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF disabled (Will be replaced by JWT)
                .csrf(csrf -> csrf.disable())

                // Session management disabled (Will be replaced by JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public routes (no token required)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/api/productos/**",
                                "/api/categorias/**",
                                "/api/proveedores/**",
                                "/uploads/**")
                        .permitAll()

                        // All other routes will require authentication
                        .anyRequest().authenticated())

                // Add our JWT filter before the standard filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Administration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Frontend allowed URL (Angular)
        config.setAllowedOrigins(List.of("http://localhost:4200"));

        // Allowed methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allowed headers
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Exposed headers (to read the token if it were sent by header)
        config.setExposedHeaders(List.of("Authorization"));

        // Allow cookies if needed (not for now)
        config.setAllowCredentials(true);

        // Apply configuration for all routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Standard password encoder (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager for login
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
