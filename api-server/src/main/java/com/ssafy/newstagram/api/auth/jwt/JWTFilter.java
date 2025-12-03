package com.ssafy.newstagram.api.auth.jwt;

import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import com.ssafy.newstagram.domain.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // request에서 Authorization 헤더 가져옴
        String authorization = request.getHeader("Authorization");

        // null 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {

            System.out.println("token null");
            filterChain.doFilter(request, response); // 다음 필터로 넘김

            return;
        }

        // 순수 토큰 추출 (Bearer 부분 제거)
        String token = authorization.split(" ")[1];
        System.out.println("token value=" + token);

        // 토큰의 만료 여부 검증
        if (jwtUtil.isExpired(token)) {

            System.out.println("token expired");
            filterChain.doFilter(request, response); // 다음 필터로 넘김

            return;
        }

        // 토큰에서 email과 role 획득
        String email = jwtUtil.getEmail(token);
        String role = jwtUtil.getRole(token);

        User user = User.builder()
                .email(email)
                .passwordHash("temppassword1234")
                .role(role)
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("Authentication = " + auth);
//        System.out.println("Principal = " + auth.getPrincipal());
//        System.out.println("Authorities = " + auth.getAuthorities());

        filterChain.doFilter(request, response);
    }
}
