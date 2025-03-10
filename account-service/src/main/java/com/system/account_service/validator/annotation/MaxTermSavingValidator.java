package com.system.account_service.validator.annotation;

import com.system.account_service.validator.MaxTermSavingConstraint;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxTermSavingConstraint.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxTermSavingValidator {
    String message() default "Maximum savings term value invalid!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
