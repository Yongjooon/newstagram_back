package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = EmailFindCodeValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailFindCode {

    String message() default "인증번호는 6자리 숫자입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
