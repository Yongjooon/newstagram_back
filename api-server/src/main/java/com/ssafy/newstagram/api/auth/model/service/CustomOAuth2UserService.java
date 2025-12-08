package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.model.dto.CustomOAuth2User;
import com.ssafy.newstagram.api.auth.model.dto.GoogleResponse;
import com.ssafy.newstagram.api.auth.model.dto.OAuth2Response;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println("==== 획득한 oAuth2User 정보 ====");
        System.out.println(oAuth2User);
        System.out.println("==============================");

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;
        if(registrationId.equals("google")){
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else{
            return null;
        }

        User existData = userRepository.findByLoginTypeAndProviderId("GOOGLE", oAuth2Response.getProviderId());

        if(existData == null){ // 최초 로그인하는 경우 => 회원가입 진행
            String dummyPassword = UUID.randomUUID().toString();
            PasswordEncoder encoder = new BCryptPasswordEncoder(); // DI 문제 생겨서 일단 직접 생성함
            String encoded = encoder.encode(dummyPassword);

            User user = User.builder()
                .email(oAuth2Response.getEmail())
                .passwordHash(encoded) // 암호화된 랜덤 더미 비밀번호
                .nickname(oAuth2Response.getName())
                .loginType("GOOGLE")
                .providerId(oAuth2Response.getProviderId())
                .build();

            userRepository.save(user);

            return new CustomOAuth2User(user);

        } // todo : email 회원가입 계정과 중복되는 경우 처리
        else{
            // todo: 가입 이후 이메일이 변경되는 경우는 나중에 고려..
            return new CustomOAuth2User(existData);
        }

    }
}
