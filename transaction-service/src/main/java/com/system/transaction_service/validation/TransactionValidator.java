package com.system.transaction_service.validation;

import com.system.transaction_service.repository.TransactionRepository;
import com.system.transaction_service.util.Constant;
import com.system.transaction_service.validation.annotation.TransactionConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RequiredArgsConstructor
public class TransactionValidator implements ConstraintValidator<TransactionConstraint, Object> {

    private final MessageSource messageSource;

    private final TransactionRepository transactionRepository;

    @Override
    public void initialize(TransactionConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        try {

            if (!transactionRepository.existsById(value.toString())) {

                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(
                                messageSource.getMessage(Constant.INVALID_TRANSACTION_ID,
                                        null, LocaleContextHolder.getLocale()))
                        .addConstraintViolation();

                return false;
            }

            return true;
        } catch (Exception e) {

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                            messageSource.getMessage(Constant.INVALID_TRANSACTION_ID,
                                    null, LocaleContextHolder.getLocale()))
                    .addConstraintViolation();

            return false;
        }
    }
}
