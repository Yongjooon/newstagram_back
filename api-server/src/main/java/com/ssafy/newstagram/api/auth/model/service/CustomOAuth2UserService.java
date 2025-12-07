package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.model.dto.CustomOAuth2User;
import com.ssafy.newstagram.api.auth.model.dto.GoogleResponse;
import com.ssafy.newstagram.api.auth.model.dto.OAuth2Response;
import com.ssafy.newstagram.domain.user.entity.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

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

        User user = User.builder()
                .email(oAuth2Response.getEmail())
                .nickname(oAuth2Response.getName())
                .loginType("GOOGLE")
                .providerId(oAuth2Response.getProviderId())
                .build();

        return new CustomOAuth2User(user);
    }
}
