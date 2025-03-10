package com.system.common_library.validation.annotation;

import com.system.common_library.util.Constant;
import com.system.common_library.validation.BirthdayValidator;
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
@Constraint(validatedBy = BirthdayValidator.class)
public @interface BirthdayConstraint {

    String message() default "{" + Constant.INVALID_BIRTHDAY + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
