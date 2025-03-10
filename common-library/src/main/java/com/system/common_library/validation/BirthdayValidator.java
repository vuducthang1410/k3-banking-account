package com.system.common_library.validation;

import com.system.common_library.util.Constant;
import com.system.common_library.validation.annotation.BirthdayConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
public class BirthdayValidator implements ConstraintValidator<BirthdayConstraint, Object> {

    private final MessageSource messageSource;

    @Override
    public void initialize(BirthdayConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        try {

            LocalDate now = LocalDate.now();
            LocalDate date = (LocalDate) value;
            if (date.isAfter(now.minusYears(18))) {

                log.info("{} is not valid date", date);
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(messageSource.getMessage
                                (Constant.INVALID_BIRTHDAY, null, LocaleContextHolder.getLocale()))
                        .addConstraintViolation();

                return false;
            }

            return true;
        } catch (Exception e) {

            log.error("{} is not valid value", value);
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(messageSource.getMessage
                            (Constant.INVALID_BIRTHDAY, null, LocaleContextHolder.getLocale()))
                    .addConstraintViolation();

            return false;
        }
    }
}
