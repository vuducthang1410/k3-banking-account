package com.system.napas_service.validation;

import com.system.napas_service.repository.BankRepository;
import com.system.napas_service.util.Constant;
import com.system.napas_service.validation.annotation.BankConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RequiredArgsConstructor
public class BankValidator implements ConstraintValidator<BankConstraint, Object> {

    private final MessageSource messageSource;

    private final BankRepository bankRepository;

    @Override
    public void initialize(BankConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        try {

            return bankRepository.existsById(value.toString());
        } catch (Exception e) {

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(messageSource.getMessage
                            (Constant.INVALID_BANK, null, LocaleContextHolder.getLocale()))
                    .addConstraintViolation();
            return false;
        }
    }
}
