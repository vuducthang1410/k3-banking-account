package org.demo.loanservice.validatedCustom.interfaceValidate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.demo.loanservice.validatedCustom.RequestStatusValidator;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequestStatusValidator.class)
@Documented
public @interface RequestStatusValidation {
    String message() default "Invalid request status (request status: APPROVED or REJECTED)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
