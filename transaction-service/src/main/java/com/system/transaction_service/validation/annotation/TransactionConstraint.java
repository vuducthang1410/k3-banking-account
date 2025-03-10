package com.system.transaction_service.validation.annotation;

import com.system.transaction_service.util.Constant;
import com.system.transaction_service.validation.TransactionValidator;
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
@Constraint(validatedBy = TransactionValidator.class)
public @interface TransactionConstraint {

    String message() default "{" + Constant.INVALID_TRANSACTION_ID + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
