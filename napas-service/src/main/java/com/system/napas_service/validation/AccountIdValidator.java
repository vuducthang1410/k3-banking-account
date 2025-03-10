package com.system.napas_service.validation;

import com.system.napas_service.repository.AccountRepository;
import com.system.napas_service.util.Constant;
import com.system.napas_service.validation.annotation.AccountIdConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RequiredArgsConstructor
public class AccountIdValidator implements ConstraintValidator<AccountIdConstraint, Object> {

    private final MessageSource messageSource;

    private final AccountRepository accountRepository;

    @Override
    public void initialize(AccountIdConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        try {

            if (accountRepository.existsByAccountId(value.toString())) {

                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(
                                messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_ID,
                                        null, LocaleContextHolder.getLocale()))
                        .addConstraintViolation();

                return false;
            }

            return true;
        } catch (Exception e) {

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                            messageSource.getMessage(Constant.INVALID_ACCOUNT_ID, null, LocaleContextHolder.getLocale()))
                    .addConstraintViolation();

            return false;
        }
    }
}
