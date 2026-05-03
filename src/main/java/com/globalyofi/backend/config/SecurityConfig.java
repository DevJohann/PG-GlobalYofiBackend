package com.globalyofi.backend.config;

import com.globalyofi.backend.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpMethod;
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

    // Removed WebSecurityCustomizer to ensure all requests pass through the
    // security filter chain
    // and receive CORS headers correctly.

    /**
     * Main security configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitir todas las peticiones OPTIONS pre-flight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Rutas públicas: GET restringido a solo ver la lista base
                        .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias", "/api/proveedores/**")
                        .permitAll()
                        .requestMatchers("/api/auth/**", "/uploads/**", "/api/chatbot/**", "/error").permitAll()

                        // REGLAS PARA PEDIDOS
                        // 1. Cualquier usuario autenticado puede realizar pedidos y ver sus propios
                        // pedidos
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/realizar").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/mis-pedidos").authenticated()
                        // 2. Solo ADMIN puede ver lista completa y detalles técnicos
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasRole("ADMIN")

                        // REGLAS PARA PAGOS
                        .requestMatchers(HttpMethod.GET, "/api/pagos/config").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/pagos/config").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pagos/config/qr").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/pagos/*/iniciar").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/pagos/*/comprobante").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pagos/*").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/pagos/*/validar").hasRole("ADMIN")

                        // REGLAS PARA USUARIOS
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // El resto de rutas requieren autenticación
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Administration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "https://pg-globalyofifrontend-production.up.railway.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
