package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private static final String PHONE_NUMBER_REGEX = "^(01[016789])\\d{3,4}\\d{4}$";
    private static final Pattern PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            addConstraintViolation(context, "휴대폰 번호를 입력해주세요.");
            return false;
        }

        if (!PATTERN.matcher(value).matches()) {
//            010, 011 등 01로 시작하는 번호만 입력 가능합니다. (하이픈 제외)
            addConstraintViolation(context, "올바른 휴대폰 번호 형식이 아닙니다.");
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