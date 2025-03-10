package com.system.transaction_service.validation;

import com.system.transaction_service.util.Constant;
import com.system.transaction_service.validation.annotation.FileConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
public class FileValidator implements ConstraintValidator<FileConstraint, Object> {

    private final MessageSource messageSource;

    @Override
    public void initialize(FileConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        try {

            if (value instanceof MultipartFile file) {

                if (file.isEmpty()) {

                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate(
                                    messageSource.getMessage(Constant.INVALID_IMAGE_FILE,
                                            null, LocaleContextHolder.getLocale()))
                            .addConstraintViolation();

                    return false;
                } else if (file.getSize() > 10 * 1024 * 1024) {

                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate(
                                    messageSource.getMessage(Constant.INVALID_IMAGE_FILE_SIZE,
                                            null, LocaleContextHolder.getLocale()))
                            .addConstraintViolation();

                    return false;
                } else if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {

                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate(
                                    messageSource.getMessage(Constant.INVALID_IMAGE_FILE_TYPE,
                                            null, LocaleContextHolder.getLocale()))
                            .addConstraintViolation();

                    return false;
                }
            }

            return true;
        } catch (Exception e) {

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                            messageSource.getMessage(Constant.INVALID_IMAGE_FILE, null, LocaleContextHolder.getLocale()))
                    .addConstraintViolation();

            return false;
        }
    }
}
