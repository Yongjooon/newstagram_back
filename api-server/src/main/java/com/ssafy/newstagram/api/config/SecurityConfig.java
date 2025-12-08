package com.ssafy.newstagram.api.config;

import com.ssafy.newstagram.api.auth.handler.CustomSuccessHandler;
import com.ssafy.newstagram.api.auth.jwt.JWTFilter;
import com.ssafy.newstagram.api.auth.jwt.JWTUtil;
import com.ssafy.newstagram.api.auth.jwt.LoginFilter;
import com.ssafy.newstagram.api.auth.model.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCrybpPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        AuthenticationManager authManager = authenticationManager();

        // JWTFilter 생성
        JWTFilter jwtFilter = new JWTFilter(jwtUtil);

        // LoginFilter 생성 + URL 설정
        LoginFilter loginFilter = new LoginFilter(authManager, jwtUtil);
        loginFilter.setFilterProcessesUrl("/auth/login"); // 로그인 URL

        // CORS 설정
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        return configuration;
                    }
                }));

        http
            .authorizeHttpRequests(auth -> auth
//                .requestMatchers("/", "/signup", "/login").permitAll()
//                .anyRequest().authenticated()
                .anyRequest().permitAll()  // 개발 중이므로 모든 요청 허용
            )
            .csrf(csrf -> csrf.disable())  // 개발 중이므로 CSRF 비활성화
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())  // H2 Console 등을 위해
            );

        // oauth2
        http.oauth2Login(oauth2 ->
                oauth2.userInfoEndpoint(userInfoEndpointConfig ->
                        userInfoEndpointConfig.userService(customOAuth2UserService)
                ).successHandler(customSuccessHandler)
        );

        // 세션 설정 : STATELESS
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, LoginFilter.class);
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
