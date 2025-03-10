package com.system.account_service.validator.annotation;

import com.system.account_service.validator.EnumValidatorConstraint;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValidatorConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@NotNull
public @interface EnumValidator {
    String message() default "Enum value invalid!";
    Class<? extends Enum<?>> enumClass();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
