package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NicknameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER}) // 2. 필드와 파라미터에 사용 가능
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNickname {

    String message() default "유효하지 않은 닉네임입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}