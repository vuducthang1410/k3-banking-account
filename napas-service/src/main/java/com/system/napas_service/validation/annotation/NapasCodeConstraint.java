package com.system.napas_service.validation.annotation;

import com.system.napas_service.util.Constant;
import com.system.napas_service.validation.NapasCodeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.METHOD,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PARAMETER,
        ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NapasCodeValidator.class)
public @interface NapasCodeConstraint {

    String message() default "{" + Constant.INVALID_NAPAS_CODE + "}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
