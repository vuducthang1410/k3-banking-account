package org.demo.loanservice.validatedCustom.interfaceValidate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.demo.loanservice.validatedCustom.FormDeftRepaymentValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FormDeftRepaymentValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormDeftRepaymentValidation {
    String message() default "Invalid value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
