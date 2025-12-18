package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailFindCodeValidator implements ConstraintValidator<EmailFindCode, String> {

    @Override
    public boolean isValid(String code, ConstraintValidatorContext context) {
        if(code == null || code.isBlank()){
            return false;
        }

        return code.matches("^\\d{6}$");
    }
}
