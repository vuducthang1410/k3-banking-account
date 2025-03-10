package com.system.napas_service.validation;

import com.system.napas_service.repository.AccountRepository;
import com.system.napas_service.util.Constant;
import com.system.napas_service.validation.annotation.AccountNumberConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RequiredArgsConstructor
public class AccountNumberValidator implements ConstraintValidator<AccountNumberConstraint, Object> {

    private final MessageSource messageSource;

    private final AccountRepository accountRepository;

    @Override
    public void initialize(AccountNumberConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        try {

            if (accountRepository.existsByAccountNumber(value.toString())) {

                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(
                                messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                                        null, LocaleContextHolder.getLocale()))
                        .addConstraintViolation();

                return false;
            }

            return true;
        } catch (Exception e) {

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                            messageSource.getMessage(Constant.INVALID_ACCOUNT_NUMBER, null, LocaleContextHolder.getLocale()))
                    .addConstraintViolation();

            return false;
        }
    }
}
