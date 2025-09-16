package com.rebra.config;

import com.rebra.annotation.LoginUser;
import com.rebra.config.resolver.LoginUserArgumentResolver;
import com.rebra.security.CustomUserDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 테스트용 시큐리티 설정
 */
@TestConfiguration
@EnableWebSecurity
public class SecurityTestConfig implements WebMvcConfigurer {

    /**
     * 테스트용 보안 설정 - 모든 요청 허용
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    /**
     * ArgumentResolver 등록
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(testLoginUserArgumentResolver());
    }

    /**
     * 테스트용 LoginUserArgumentResolver를 @Primary로 오버라이드
     */
    @Bean
    @Primary
    public LoginUserArgumentResolver testLoginUserArgumentResolver() {
        return new LoginUserArgumentResolver() {
            @Override
            public Object resolveArgument(MethodParameter parameter,
                                        ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest,
                                        WebDataBinderFactory binderFactory) throws Exception {

                // CustomUserDetails를 직접 생성하여 SecurityContext에 설정
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                CustomUserDetails userDetails = new CustomUserDetails(1L, "testuser", authorities);

                // Authentication 객체 생성 및 SecurityContext에 설정
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                return 1L;
            }
        };
    }
}