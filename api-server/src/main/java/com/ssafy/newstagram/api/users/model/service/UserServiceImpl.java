package com.ssafy.newstagram.api.users.model.service;

import com.ssafy.newstagram.api.auth.model.service.VerificationCodeService;
import com.ssafy.newstagram.api.users.model.dto.*;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements  UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;

    @Override
    public void register(RegisterRequestDto dto) {

        // 휴대폰 인증여부 체크
        String phoneNumber = dto.getPhoneNumber();
        if(!verificationCodeService.checkVerified(phoneNumber)){
            throw new IllegalArgumentException("휴대폰 인증을 진행해주세요.");
        }

        // 이메일 중복 체크
        if(userRepository.existsByEmailIncludeDeleted(dto.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // User 객체 생성
        User user = User.builder()
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .passwordHash(encodedPassword)
                .nickname(dto.getNickname())
                .build();

        userRepository.save(user);

        verificationCodeService.deletePhoneVerificationKey(phoneNumber);
    }

    @Override
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );
        userRepository.delete(user); // soft delete
    }

    @Override
    public UserInfoDto getUserInfoByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );

        return UserInfoDto.builder()
            .email(user.getEmail())
            .nickname(user.getNickname())
            .role(user.getRole())
            .preferenceEmbedding(user.getPreferenceEmbedding())
            .build();
    }

    @Override
    public void updateNickname(Long userId, UpdateNicknameRequestDto dto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );

        user.updateNickname(dto.getNewNickname());
        userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
            () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );
    }

    @Transactional
    @Override
    public void updatePassword(Long userId, UpdatePasswordRequestDto dto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );

        if(!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String newEncoded = passwordEncoder.encode(dto.getNewPassword());

        user.updatePasswordHash(newEncoded);
    }

    @Override
    public boolean isAvailableEmail(EmailAvailabilityRequestDto dto) {
        String email = dto.getEmail();
        return !userRepository.existsByEmailIncludeDeleted(email);
    }

    @Override
    public boolean isAvailablePhoneNumber(PhoneNumberAvailabilityRequestDto dto) {
        String phoneNumber = dto.getPhoneNumber();
        return !userRepository.existsByPhoneNumberIncludedDeleted(phoneNumber);
    }

    @Override
    public boolean isAvailableNickname(NicknameAvailabilityRequestDto dto) {
        String nickname = dto.getNickname();
        return !userRepository.existsByNicknameIncludedDeleted(nickname);
    }
}
