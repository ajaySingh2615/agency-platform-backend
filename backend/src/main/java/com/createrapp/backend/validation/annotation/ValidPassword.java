package com.createrapp.backend.validation.annotation;

import com.createrapp.backend.validation.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface ValidPassword {

    String message() default "Password does not meet requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
