package com.system.napas_service.validation;

import com.system.napas_service.repository.BankRepository;
import com.system.napas_service.util.Constant;
import com.system.napas_service.validation.annotation.NapasCodeConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RequiredArgsConstructor
public class NapasCodeValidator implements ConstraintValidator<NapasCodeConstraint, Object> {

    private final MessageSource messageSource;

    private final BankRepository bankRepository;

    @Override
    public void initialize(NapasCodeConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        try {

            if (bankRepository.existsByNapasCode(value.toString())) {

                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(
                                messageSource.getMessage(Constant.DUPLICATE_NAPAS_CODE,
                                        null, LocaleContextHolder.getLocale()))
                        .addConstraintViolation();

                return false;
            }

            return true;
        } catch (Exception e) {

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                            messageSource.getMessage(
                                    Constant.INVALID_NAPAS_CODE, null, LocaleContextHolder.getLocale()))
                    .addConstraintViolation();

            return false;
        }
    }
}
