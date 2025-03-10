package com.system.transaction_service.validation.annotation;

import com.system.transaction_service.util.Constant;
import com.system.transaction_service.validation.SwiftCodeValidator;
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
@Constraint(validatedBy = SwiftCodeValidator.class)
public @interface SwiftCodeConstraint {

    String message() default "{" + Constant.INVALID_SWIFT_CODE + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
