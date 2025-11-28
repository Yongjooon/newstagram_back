package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false;
        }

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%].*"); // !,@,#,$,% 만 허용

        int count = 0;
        if (hasLetter) count++;
        if (hasDigit) count++;
        if (hasSpecial) count++;

        return count >= 2;
    }
}
