package com.ssafy.newstagram.api.users.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = PasswordValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "비밀번호는 영문/숫자/특수문자(!,@,#,$,%) 중 두 가지 이상 포함해야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
