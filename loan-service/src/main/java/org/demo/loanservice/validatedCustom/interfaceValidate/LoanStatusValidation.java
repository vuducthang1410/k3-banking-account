package org.demo.loanservice.validatedCustom.interfaceValidate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.demo.loanservice.validatedCustom.LoanStatusValidator;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoanStatusValidator.class)
@Documented
public @interface LoanStatusValidation {
    String message() default "Invalid loan status";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
