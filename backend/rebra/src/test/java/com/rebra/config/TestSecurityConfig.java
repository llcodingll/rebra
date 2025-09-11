package com.rebra.config;

import com.rebra.jwt.JwtAuthenticationFilter;
import com.rebra.jwt.TokenProvider;
import com.rebra.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http, 
                                               @Autowired(required = false) TokenProvider tokenProvider,
                                               @Autowired(required = false) TokenService tokenService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                );

        // TokenProvider와 TokenService가 있을 때만 JWT 필터 추가
        if (tokenProvider != null && tokenService != null) {
            http.addFilterBefore(new JwtAuthenticationFilter(tokenProvider, tokenService),
                    UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
