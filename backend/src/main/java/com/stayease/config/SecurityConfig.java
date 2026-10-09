package com.stayease.config;

import com.stayease.security.JwtAuthFilter;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.http.HttpStatus;
import java.util.*;

@Configuration
public class SecurityConfig {
    private final JwtAuthFilter filter;

    public SecurityConfig(JwtAuthFilter f) {
        filter = f;
    }

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable()).cors(c -> c.configurationSource(cors())).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
            System.err.println("[SECURITY 401] " + req.getMethod() + " " + req.getRequestURI() + ": " + e.getMessage());
            res.sendError(HttpStatus.UNAUTHORIZED.value(), "Authentication required");
        }).accessDeniedHandler((req, res, e) -> {
            System.err.println("[SECURITY 403] " + req.getMethod() + " " + req.getRequestURI() + ": " + e.getMessage());
            res.sendError(HttpStatus.FORBIDDEN.value(), "Access denied");
        })).authorizeHttpRequests(a -> a.requestMatchers("/api/auth/register", "/api/auth/login", "/h2-console/**").permitAll().requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated().requestMatchers(HttpMethod.POST, "/api/hotels").hasRole("ADMIN").requestMatchers(HttpMethod.PUT, "/api/hotels/*").hasRole("ADMIN").requestMatchers(HttpMethod.DELETE, "/api/hotels/*").hasRole("ADMIN").requestMatchers(HttpMethod.POST, "/api/hotels/*/rooms").hasRole("MANAGER").requestMatchers("/api/rooms/**").hasRole("MANAGER").requestMatchers("/api/manager/**").hasRole("MANAGER").requestMatchers("/api/bookings/mine", "/api/bookings", "/api/bookings/*/cancel").hasRole("GUEST").anyRequest().permitAll()).headers(h -> h.frameOptions(f -> f.sameOrigin())).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource cors() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}
