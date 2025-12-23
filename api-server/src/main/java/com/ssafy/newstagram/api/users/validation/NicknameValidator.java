package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class NicknameValidator implements ConstraintValidator<ValidNickname, String> {

    private static final String REGEX = "^[a-zA-Z0-9가-힣_-]+$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext context) {

        if (nickname == null || nickname.isBlank()) {
            addConstraintViolation(context, "닉네임을 입력해주세요.");
            return false;
        }

        int length = nickname.length();
        if (length < 2 || length > 50) {
            addConstraintViolation(context, "닉네임은 2자 이상 50자 이하여야 합니다.");
            return false;
        }

        if (!PATTERN.matcher(nickname).matches()) {
            addConstraintViolation(context, "닉네임은 영문, 한글, 숫자, 언더바(_), 대시(-)만 사용할 수 있습니다.");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String errorMessage) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(errorMessage)
                .addConstraintViolation();
    }
}