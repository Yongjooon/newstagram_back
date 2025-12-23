package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.model.dto.EmailFindRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.EmailFindVerifyRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.PhoneVerificationConfirmDto;
import com.ssafy.newstagram.api.auth.model.dto.PhoneVerificationRequestDto;
import com.ssafy.newstagram.api.exception.VerificationException;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService{
    private final UserRepository userRepository;
    private final SmsService smsService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    private final String EMAIL_FIND_CODE_PREFIX = "email-find:";
    private final String PHONE_VERIFICATION_CODE_PREFIX = "phone-verification:";
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public void requestEmailFindVerificationCode(EmailFindRequestDto dto, long expirationMs) {
        String phoneNumber = dto.getPhoneNumber();
        long userId = userRepository.findIdByPhoneNumber(phoneNumber).orElseThrow(
                () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );

        String key = generateEmailFindKey(phoneNumber);
        String code = generateCode();
        redisTemplate.opsForHash().put(key, "code", code);
        redisTemplate.opsForHash().put(key, "attempts", MAX_ATTEMPTS);
        redisTemplate.opsForHash().put(key, "userId", String.valueOf(userId));

        redisTemplate.expire(key, expirationMs, TimeUnit.MILLISECONDS);

        log.info("[Auth] Generate email find code: phoneNumber={} userId={} code={}", phoneNumber, userId, code);

        String msg = "[Newstagram] 인증번호: " + code;
        System.out.println(msg);

        smsService.send(phoneNumber, msg);
    }

    @Override
    public String verifyAndGetEmail(EmailFindVerifyRequestDto dto) {
        String inputCode = dto.getVerificationCode();
        String key = generateEmailFindKey(dto.getPhoneNumber());

        String code = (String) redisTemplate.opsForHash().get(key, "code");
        Integer attempts = (Integer) redisTemplate.opsForHash().get(key, "attempts");
        String userIdStr = (String) redisTemplate.opsForHash().get(key, "userId");

        System.out.println("code" + code);
        System.out.println(attempts);
        System.out.println(userIdStr);

        if (code == null || attempts == null || userIdStr == null) {
            throw new VerificationException("인증 정보가 만료되었거나 존재하지 않습니다.");
        }
        Long userId = Long.parseLong(userIdStr);


        int remainingAttempts = (int) attempts;
        if (remainingAttempts <= 0) {
            redisTemplate.delete(key);
            throw new VerificationException("시도 횟수를 초과하였습니다. 인증 요청을 다시 해주세요.");
        }

        if (!code.equals(inputCode)) {
            redisTemplate.opsForHash().put(key, "attempts", remainingAttempts - 1);
            throw new VerificationException("만료되거나 유효하지 않은 인증번호입니다.");
        }

        redisTemplate.delete(key);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        return user.getEmail();
    }

    @Override
    public void requestPhoneVerificationCode(PhoneVerificationRequestDto dto, long expirationMs) {
        String phoneNumber = dto.getPhoneNumber();

        if(userRepository.existsByPhoneNumberIncludedDeleted(phoneNumber)){
            throw new IllegalArgumentException("이미 가입된 번호입니다.");
        }

        String key = generatePhoneVerificationKey(phoneNumber);
        String code = generateCode();
        redisTemplate.opsForHash().put(key, "code", code);
        redisTemplate.opsForHash().put(key, "attempts", MAX_ATTEMPTS);

        redisTemplate.expire(key, expirationMs, TimeUnit.MILLISECONDS);

        String msg = "[Newstagram] 인증번호: " + code;
        System.out.println(msg);

        smsService.send(phoneNumber, msg);
    }

    @Override
    public void confirmPhoneVerification(PhoneVerificationConfirmDto dto) {
        String phoneNumber = dto.getPhoneNumber();
        String inputCode = dto.getVerificationCode();

        String key = generatePhoneVerificationKey(phoneNumber);

        String code = (String) redisTemplate.opsForHash().get(key, "code");
        Integer attempts = (Integer) redisTemplate.opsForHash().get(key, "attempts");

        if (code == null || attempts == null) {
            throw new VerificationException("인증 정보가 만료되었거나 존재하지 않습니다.");
        }

        int remainingAttempts = (int) attempts;
        if (remainingAttempts <= 0) {
            redisTemplate.delete(key);
            throw new VerificationException("시도 횟수를 초과하였습니다. 인증 요청을 다시 해주세요.");
        }

        if (!code.equals(inputCode)) {
            redisTemplate.opsForHash().put(key, "attempts", remainingAttempts - 1);
            throw new VerificationException("만료되거나 유효하지 않은 인증번호입니다.");
        }

        redisTemplate.delete(key);

        redisTemplate.opsForHash().put(key, "verified", true);
        redisTemplate.expire(key, 3600000, TimeUnit.MILLISECONDS);
    }

    private String generateEmailFindKey(String phoneNumber){
        return EMAIL_FIND_CODE_PREFIX + phoneNumber;
    }
    private String generatePhoneVerificationKey(String phoneNumber){
        return PHONE_VERIFICATION_CODE_PREFIX + phoneNumber;
    }
    private String generateCode(){
        int number = random.nextInt((int) Math.pow(10, CODE_LENGTH));
        return String.format("%0" + CODE_LENGTH + "d", number);
    }

    public boolean checkVerified(String phoneNumber){
        String key = generatePhoneVerificationKey(phoneNumber);
        Boolean value = (Boolean) redisTemplate.opsForHash().get(key, "verified");
        return Boolean.TRUE.equals(value);
    }

    @Override
    public void deletePhoneVerificationKey(String phoneNumber) {
        String key = generatePhoneVerificationKey(phoneNumber);
        redisTemplate.delete(key);
    }
}
