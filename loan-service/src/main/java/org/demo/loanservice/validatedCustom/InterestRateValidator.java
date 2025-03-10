package org.demo.loanservice.validatedCustom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.demo.loanservice.validatedCustom.interfaceValidate.InterestRateValidation;

public class InterestRateValidator implements ConstraintValidator<InterestRateValidation, Double> {
    @Override
    public boolean isValid(Double value, ConstraintValidatorContext constraintValidatorContext) {
        return value>0;
    }
}
