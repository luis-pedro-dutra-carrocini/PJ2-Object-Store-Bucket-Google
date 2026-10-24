package com.restaurante.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Bean que disponibiliza o BCrypt para ser usado no sistema
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configuração para NÃO bloquear as rotas (liberar tudo para testes locais)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF para facilitar testes com fetch no frontend
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permite acesso a todos os endpoints
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // Permite o console do H2 (se usar)
            
        return http.build();
    }
}