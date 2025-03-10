package org.demo.loanservice.validatedCustom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.demo.loanservice.dto.enumDto.RequestStatus;
import org.demo.loanservice.validatedCustom.interfaceValidate.RequestStatusValidation;

import java.util.stream.Stream;

public class RequestStatusValidator implements ConstraintValidator<RequestStatusValidation, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null &&
                Stream.of(RequestStatus.APPROVED.name(),
                                RequestStatus.REJECTED.name(),
                                RequestStatus.PENDING.name())
                        .anyMatch(e -> e.equalsIgnoreCase(value));
    }
}
