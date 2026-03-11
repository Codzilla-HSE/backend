package com.codzilla.backend.config;

import com.codzilla.backend.AdminAccessDeniedHandler;
import com.codzilla.backend.HttpStatusEntryPoint;
import com.codzilla.backend.JWTRequestFilter.JWTRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] WHITELIST = {"/auth/**"};


    private final HttpStatusEntryPoint unauthorizedHandler;
    private final AdminAccessDeniedHandler adminAccessDeniedHandler;

    public SecurityConfig(HttpStatusEntryPoint unauthorizedHandler, AdminAccessDeniedHandler adminAccessDeniedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.adminAccessDeniedHandler = adminAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JWTRequestFilter filter) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Arrays.asList("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(adminAccessDeniedHandler)
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(WHITELIST).permitAll().anyRequest().authenticated())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


}