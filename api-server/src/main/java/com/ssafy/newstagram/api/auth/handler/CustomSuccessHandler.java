package com.ssafy.newstagram.api.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.api.auth.jwt.JWTUtil;
import com.ssafy.newstagram.api.auth.model.dto.CustomOAuth2User;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.auth.model.service.RefreshTokenService;
import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.users.model.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    @Value("${front-url}")
    private String FRONT_URL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        Long userId = customUserDetails.getUserId();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createAccessToken(userId, role);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        refreshTokenService.save(userId, refreshToken);

        LoginResponseDto responseDto = new LoginResponseDto(accessToken, refreshToken, role);
        String targetUrl = FRONT_URL + "/user/oauth/google";

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(targetUrl);

        Map<String, Object> params = objectMapper.convertValue(responseDto, Map.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                uriBuilder.queryParam(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        String redirectUrl = uriBuilder
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        System.out.println("redirect url: " + redirectUrl);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
