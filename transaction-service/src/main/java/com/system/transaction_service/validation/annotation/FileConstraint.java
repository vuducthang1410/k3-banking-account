package com.system.transaction_service.validation.annotation;

import com.system.transaction_service.util.Constant;
import com.system.transaction_service.validation.FileValidator;
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
@Constraint(validatedBy = FileValidator.class)
public @interface FileConstraint {

    String message() default "{" + Constant.INVALID_IMAGE_FILE + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
