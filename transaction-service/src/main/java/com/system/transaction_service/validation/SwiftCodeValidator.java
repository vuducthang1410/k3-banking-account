package com.system.transaction_service.validation;

import com.system.transaction_service.repository.ExternalBankRepository;
import com.system.transaction_service.util.Constant;
import com.system.transaction_service.validation.annotation.SwiftCodeConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RequiredArgsConstructor
public class SwiftCodeValidator implements ConstraintValidator<SwiftCodeConstraint, Object> {

    private final MessageSource messageSource;

    private final ExternalBankRepository externalBankRepository;

    @Override
    public void initialize(SwiftCodeConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        try {

            if (externalBankRepository.existsBySwiftCode(value.toString())) {

                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(
                                messageSource.getMessage(Constant.DUPLICATE_SWIFT_CODE,
                                        null, LocaleContextHolder.getLocale()))
                        .addConstraintViolation();

                return false;
            }

            return true;
        } catch (Exception e) {

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                            messageSource.getMessage(Constant.INVALID_SWIFT_CODE, null, LocaleContextHolder.getLocale()))
                    .addConstraintViolation();

            return false;
        }
    }
}
