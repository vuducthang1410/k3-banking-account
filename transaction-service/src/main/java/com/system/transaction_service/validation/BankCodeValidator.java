package com.system.transaction_service.validation;

import com.system.transaction_service.repository.ExternalBankRepository;
import com.system.transaction_service.util.Constant;
import com.system.transaction_service.validation.annotation.BankCodeConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RequiredArgsConstructor
public class BankCodeValidator implements ConstraintValidator<BankCodeConstraint, Object> {

    private final MessageSource messageSource;

    private final ExternalBankRepository externalBankRepository;

    @Override
    public void initialize(BankCodeConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        try {

            if (externalBankRepository.existsByCode(value.toString())) {

                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(
                                messageSource.getMessage(Constant.DUPLICATE_BANK_CODE,
                                        null, LocaleContextHolder.getLocale()))
                        .addConstraintViolation();

                return false;
            }

            return true;
        } catch (Exception e) {

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                            messageSource.getMessage(Constant.INVALID_BANK_CODE, null, LocaleContextHolder.getLocale()))
                    .addConstraintViolation();

            return false;
        }
    }
}
