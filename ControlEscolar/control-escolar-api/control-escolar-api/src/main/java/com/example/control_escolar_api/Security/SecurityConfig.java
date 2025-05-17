package com.example.control_escolar_api.Security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("jwtFilterBean") JwtFilter jwtFilter
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // 🔒 Evitar bloqueos tipo 403
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login").permitAll() // 🔓 Permitir login sin token
                        .anyRequest().authenticated()              // 🔐 Lo demás protegido
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.disable()); // ❗ Sin sesiones

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
