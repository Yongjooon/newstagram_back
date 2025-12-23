package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_REGEX = "^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,6}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {

        if (email == null || email.isBlank()) {
            addConstraintViolation(context, "이메일을 입력해주세요.");
            return false;
        }

        if (!PATTERN.matcher(email).matches()) {
            addConstraintViolation(context, "올바른 이메일 형식이 아닙니다.");
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