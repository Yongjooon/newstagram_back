package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneVerificationCodeValidator implements ConstraintValidator<ValidPhoneVerificationCode, String> {

    private static final String CODE_REGEX = "^\\d{6}$";
    private static final Pattern PATTERN = Pattern.compile(CODE_REGEX);

    @Override
    public boolean isValid(String code, ConstraintValidatorContext context) {

        if (code == null || code.isBlank()) {
            addConstraintViolation(context, "인증번호를 입력해주세요.");
            return false;
        }

        if (!PATTERN.matcher(code).matches()) {
            addConstraintViolation(context, "인증번호는 6자리 숫자여야 합니다.");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String msg) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(msg)
                .addConstraintViolation();
    }
}