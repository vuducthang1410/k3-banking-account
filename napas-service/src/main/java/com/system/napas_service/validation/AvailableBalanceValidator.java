package com.system.napas_service.validation;

import com.system.napas_service.dto.account.CreateAccountDTO;
import com.system.napas_service.util.Constant;
import com.system.napas_service.validation.annotation.AvailableBalanceConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RequiredArgsConstructor
public class AvailableBalanceValidator implements ConstraintValidator<AvailableBalanceConstraint, Object> {

    private final MessageSource messageSource;

    @Override
    public void initialize(AvailableBalanceConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {

        try {

            if (object instanceof CreateAccountDTO create) {

                if (create.getAvailableBalance().compareTo(create.getBalance()) > 0) {

                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate(messageSource.getMessage
                                    (Constant.EXCEED_AVAILABLE_BALANCE, null, LocaleContextHolder.getLocale()))
                            .addPropertyNode("availableBalance")
                            .addConstraintViolation();

                    return false;
                }
            }

            return true;
        } catch (Exception e) {

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(messageSource.getMessage
                            (Constant.INVALID_AVAILABLE_BALANCE, null, LocaleContextHolder.getLocale()))
                    .addPropertyNode("availableBalance")
                    .addConstraintViolation();

            return false;
        }
    }
}
