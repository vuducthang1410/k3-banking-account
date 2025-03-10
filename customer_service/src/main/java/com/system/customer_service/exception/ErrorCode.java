package com.system.customer_service.exception;

import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Locale;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "error.uncategorized", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "error.invalid_key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "error.user_existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "error.username_invalid", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "error.invalid_password", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "error.notfound", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "err.unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "err.unauthorized", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "error.invalid_dob", HttpStatus.BAD_REQUEST),
    PHONE_MANDATORY(1009, "phone.mandatory", HttpStatus.BAD_REQUEST),
    VALIDATION_PHONE_INVALID(1010, "validation.phone.invalid", HttpStatus.BAD_REQUEST),
    FIRSTNAME_MANDATORY(1011, "firstname.mandatory", HttpStatus.BAD_REQUEST),
    TOKEN_MANDATORY(1012, "token.mandatory", HttpStatus.BAD_REQUEST),
    LASTNAME_MANDATORY(1013, "lastname.mandatory", HttpStatus.BAD_REQUEST),
    ADDRESS_MANDATORY(1014, "address.mandatory", HttpStatus.BAD_REQUEST),
    IDENTITY_CARD_MANDATORY(1015, "identityCard.mandatory", HttpStatus.BAD_REQUEST),
    DOB_MANDATORY(1016, "dob.mandatory", HttpStatus.BAD_REQUEST),
    GENDER_MANDATORY(1017, "gender.mandatory", HttpStatus.BAD_REQUEST),
    PLACE_ORIGIN_MANDATORY(1018, "placeOrigin.mandatory", HttpStatus.BAD_REQUEST),
    VALIDATION_PASSWORD_PATTERN(1020, "validation.password.pattern", HttpStatus.BAD_REQUEST),
    VALIDATION_EMAIL_NOT_BLANK(1021, "validation.email.notblank", HttpStatus.BAD_REQUEST),
    VALIDATION_EMAIL_INVALID(1022, "validation.email.invalid", HttpStatus.BAD_REQUEST),
    IDENTITY_CARD_EXACT(1023, "identityCard.exact", HttpStatus.BAD_REQUEST),
    USER_PHONE_EXISTED(1024, "userPhone.existed", HttpStatus.CONFLICT),
    USER_IDCARD_EXISTED(1025, "userIdCard.existed", HttpStatus.CONFLICT),
    USER_MAIL_EXISTED(1026, "userMail.existed", HttpStatus.CONFLICT),
    PASSWORD_MANDATORY(1027, "password.mandatory", HttpStatus.BAD_REQUEST),
    GENDER_INVALID(1028, "gender.invalid", HttpStatus.BAD_REQUEST),
    KYC_NOT_APPROVE(1029, "kyc.notApprove", HttpStatus.FORBIDDEN),
    ACCOUNT_SERVICE_DOWN(1030, "accountService.down", HttpStatus.SERVICE_UNAVAILABLE),
    NOTIFICATION_SERVICE_DOWN(1031, "notificationService.down", HttpStatus.SERVICE_UNAVAILABLE),
    CORE_BANKING_SERVICE_DOWN(1032, "coreBankingService.down", HttpStatus.SERVICE_UNAVAILABLE),
    CUSTOMER_ATTEMPTS(1033, "customer.Attempts", HttpStatus.TOO_MANY_REQUESTS),
    CUSTOMER_CREATE_SUCCESS(1034, "customerCreate.success", HttpStatus.CREATED),
    VERIFY_MAIL_SUCCESS(1035, "verifyMail.success", HttpStatus.OK),
    VERIFY_MAIL_FAIL(1036, "verifyMail.fail", HttpStatus.BAD_REQUEST),
    VERIFY_CODE_INVALID(1037, "verifyCode.Invalid", HttpStatus.BAD_REQUEST),
    VERIFY_CODE_NOTFOUND(1038, "verifyCode.Notfound", HttpStatus.GONE),
    GENERATE_VERIFICATION_CODE_SUCCESS(1039, "generateVerificationCode.success", HttpStatus.OK),
    GENERATE_VERIFICATION_CODE_FAIL(1040, "generateVerificationCode.fail", HttpStatus.INTERNAL_SERVER_ERROR),
    LOGIN_VERIFY_MAIL(1041, "login.verifyMail", HttpStatus.UNAUTHORIZED),
    KYC_NOT_FOUND(1042,"kyc.notFound",HttpStatus.NOT_FOUND),
    ACCOUNT_SUSPENDED(1043, "account.suspended", HttpStatus.BAD_REQUEST),
    ACCOUNT_CLOSED(1044, "account.closed", HttpStatus.BAD_REQUEST),
    PASSWORD_ERROR(1045, "password.error", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_CONFIRM(1046, "password.not.confirm", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_FOUND(1047, "permission.not.found", HttpStatus.NOT_FOUND),
    ERROR_CODE(1048, "otp.not.true", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCK(1049, "account.locked", HttpStatus.BAD_REQUEST),
    LOGOUT_FAILED(1050, "logout.failed", HttpStatus.BAD_REQUEST),

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    public String getMessage(MessageSource messageSource, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(this.message, args, locale);
    }
}
