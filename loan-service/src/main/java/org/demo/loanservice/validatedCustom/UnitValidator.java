package org.demo.loanservice.validatedCustom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.demo.loanservice.dto.enumDto.Unit;
import org.demo.loanservice.validatedCustom.interfaceValidate.UnitValidation;

import java.util.stream.Stream;

public class UnitValidator implements ConstraintValidator<UnitValidation, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        return Stream.of(Unit.DATE.name(), Unit.MONTH.name(), Unit.YEAR.name())
                .anyMatch(name -> name.equalsIgnoreCase(value.toUpperCase()));
    }
}
