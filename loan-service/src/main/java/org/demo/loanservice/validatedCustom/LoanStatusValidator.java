package org.demo.loanservice.validatedCustom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.demo.loanservice.dto.enumDto.LoanStatus;
import org.demo.loanservice.validatedCustom.interfaceValidate.LoanStatusValidation;

import java.util.stream.Stream;

public class LoanStatusValidator implements ConstraintValidator<LoanStatusValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null &&
                Stream.of(LoanStatus.ACTIVE.name(),
                                LoanStatus.PENDING.name(), LoanStatus.REJECTED.name(),
                                LoanStatus.PAID_OFF.name())
                        .anyMatch(e -> e.equalsIgnoreCase(value));
    }
}
