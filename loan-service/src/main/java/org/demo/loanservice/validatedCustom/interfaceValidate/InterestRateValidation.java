package org.demo.loanservice.validatedCustom.interfaceValidate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.demo.loanservice.validatedCustom.InterestRateValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = InterestRateValidator.class)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface InterestRateValidation {
    String message() default "Interest rate must be greater than or equal to 0";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
