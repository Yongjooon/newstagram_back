package com.ssafy.newstagram.api.auth.model.dto;

import com.ssafy.newstagram.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final User user;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                return user.getRole();
            }

        });

        return collection;
    }

    @Override
    public String getName() { // ????
        return user.getEmail();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getNickname(){
        return user.getNickname();
    }

    public Long getUserId() { return user.getId(); }
}
