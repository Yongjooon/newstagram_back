package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_SIZE = 8;
    private static final int MAX_SIZE = 64;

    private static final String ALLOWED_CHAR_REGEX = "^[a-zA-Z0-9!@#$%]+$"; // 허용된 문자만 있는지
    private static final Pattern ALLOWED_PATTERN = Pattern.compile(ALLOWED_CHAR_REGEX);

    private static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if (password == null || password.isBlank()) {
            addConstraintViolation(context, "비밀번호를 입력해주세요.");
            return false;
        }

        if (password.length() < MIN_SIZE || password.length() > MAX_SIZE) {
            addConstraintViolation(context, String.format("비밀번호는 %d자 이상 %d자 이하여야 합니다.", MIN_SIZE, MAX_SIZE));
            return false;
        }

        if (!ALLOWED_PATTERN.matcher(password).matches()) {
            addConstraintViolation(context, "비밀번호는 영문, 숫자, 특수문자(!@#$%)만 사용할 수 있습니다.");
            return false;
        }

        int categories = 0;
        if (LETTER_PATTERN.matcher(password).find()) categories++;
        if (DIGIT_PATTERN.matcher(password).find()) categories++;
        if (SPECIAL_PATTERN.matcher(password).find()) categories++;

        if (categories < 2) {
            addConstraintViolation(context, "비밀번호는 영문/숫자/특수문자(!@#$%) 중 2가지 이상을 포함해야 합니다.");
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