package com.ssafy.newstagram.api.users.model.service;


import com.ssafy.newstagram.api.auth.model.dto.LoginRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.users.model.dto.RegisterRequestDto;
import com.ssafy.newstagram.api.users.model.dto.UpdateNicknameRequestDto;
import com.ssafy.newstagram.api.users.model.dto.UserInfoDto;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements  UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequestDto dto) {

        // 이메일 중복 체크
        if(userRepository.existsByEmailIncludeDeleted(dto.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // User 객체 생성
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(encodedPassword)
                .nickname(dto.getNickname())
                .build();

        userRepository.save(user);
    }

    @Override
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email);

        if(user == null){
            throw new IllegalArgumentException("회원을 찾을 수 없습니다.");
        }

        userRepository.delete(user); // soft delete
    }

    @Override
    public UserInfoDto getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email);

        if(user == null){
            throw new IllegalArgumentException("회원을 찾을 수 없습니다.");
        }

        return UserInfoDto.builder()
            .email(user.getEmail())
            .nickname(user.getNickname())
            .role(user.getRole())
            .preferenceEmbedding(user.getPreferenceEmbedding())
            .build();
    }

    @Override
    public void updateNickname(String email, UpdateNicknameRequestDto dto) {
        User user = userRepository.findByEmail(email);

        if(user == null){
            throw new IllegalArgumentException("회원을 찾을 수 없습니다.");
        }

        user.updateNickname(dto.getNewNickname());
        userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);

        if(user == null){
            throw new IllegalArgumentException("회원을 찾을 수 없습니다.");
        }

        return user;
    }

}
