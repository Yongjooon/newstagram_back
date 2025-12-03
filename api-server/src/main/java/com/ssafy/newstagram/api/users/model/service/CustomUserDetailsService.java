package com.ssafy.newstagram.api.users.model.service;

import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User userData = userRepository.findByEmail(email); // 요청 데이터의 email로 DB에서 조회

        if(userData == null){
            throw new UsernameNotFoundException("유저 없음: " + email);
        }

        return new CustomUserDetails(userData);
    }
}
