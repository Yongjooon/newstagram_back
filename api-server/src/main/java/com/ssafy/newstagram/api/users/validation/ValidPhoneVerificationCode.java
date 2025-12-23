package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER}) // 적용 범위 명시
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneVerificationCodeValidator.class)
public @interface ValidPhoneVerificationCode {

    String message() default "유효하지 않은 인증번호 형식입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}