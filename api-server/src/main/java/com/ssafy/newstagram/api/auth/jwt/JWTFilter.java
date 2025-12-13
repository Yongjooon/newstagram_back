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

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response); // 다음 필터로 넘김
            return;
        }

        String token = authorization.split(" ")[1];
        System.out.println("token value=" + token);

        try {
            if (jwtUtil.isExpired(token)) {
                sendJwtError(response, "AUTH_401", "토큰이 만료되었습니다.");
                return;
            }

            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            User user = User.builder()
                    .id(userId)
                    .passwordHash("temppassword1234")
                    .role(role)
                    .build();

            CustomUserDetails customUserDetails = new CustomUserDetails(user);

            // 스프링 시큐리티 인증 토큰 생성
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    customUserDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);

        } catch (io.jsonwebtoken.security.SignatureException e) {
            sendJwtError(response, "AUTH_401", "유효하지 않은 토큰입니다.");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            sendJwtError(response, "AUTH_401", "토큰이 만료되었습니다.");
        } catch (Exception e) {
            sendJwtError(response, "AUTH_500", "토큰 처리 중 서버 오류가 발생했습니다.");
        }
    }

    private void sendJwtError(HttpServletResponse response,
                              String code,
                              String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        com.ssafy.newstagram.api.common.BaseResponse<?> body =
                com.ssafy.newstagram.api.common.BaseResponse.error(
                        code,
                        message,
                        null
                );

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(body));
    }

}
