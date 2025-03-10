package com.system.account_service.validator;

import com.system.account_service.validator.annotation.MaxTermSavingValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxTermSavingConstraint implements ConstraintValidator<MaxTermSavingValidator, Integer> {
    @Override
    public void initialize(MaxTermSavingValidator constraintAnnotation) {}

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value == -1 || (value >= 1 && value <= 24);
    }
}
